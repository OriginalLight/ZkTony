package com.zktony.android.utils.ext

import com.zktony.android.utils.AsyncHelper
import com.zktony.android.utils.SerialHelper
import com.zktony.android.utils.model.ControlType
import com.zktony.android.utils.model.ExceptionPolicy
import com.zktony.android.utils.model.ExecuteType
import com.zktony.android.utils.model.SerialParams
import com.zktony.serialport.command.Protocol
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicLong

val serialHelper: SerialHelper = SerialHelper.instance
val asyncHelper: AsyncHelper = AsyncHelper.instance

private var y: AtomicLong = AtomicLong(0L)
private var z: AtomicLong = AtomicLong(0L)

fun <T : Number> pulse(index: Int, dvp: T): Long {

    val p = when (dvp) {
        is Double -> {
            (dvp / asyncHelper.hpc[index]!!).toLong()
        }

        is Long -> {
            dvp
        }

        else -> {
            dvp.toLong()
        }
    }

    return when (index) {
        0 -> {
            val d = p - y.get()
            y.set(maxOf(p, 0))
            d
        }

        1 -> {
            val d = p - z.get()
            z.set(maxOf(p, 0))
            d
        }

        else -> p
    }
}

fun sendProtocol(block: Protocol.() -> Unit) = serialHelper.sendProtocol(Protocol().apply(block))
fun setLock(ids: List<Int>, isLock: Boolean = true) = ids.forEach { serialHelper.axis[it] = isLock }
fun setLock(vararg ids: Int, isLock: Boolean = true) =
    ids.forEach { serialHelper.axis[it] = isLock }

fun getLock(ids: List<Int>) = ids.any { serialHelper.axis[it] }
fun getLock(vararg ids: Int) = ids.any { serialHelper.axis[it] }
fun getGpio(ids: List<Int>) = ids.any { serialHelper.gpio[it] }
fun getGpio(vararg ids: Int) = ids.any { serialHelper.gpio[it] }
suspend fun serial(block: SerialParams.() -> Unit) {
    // 构建命令
    val tx = SerialParams().apply(block)

    // 根据控制类型执行相应的操作
    when (tx.controlType) {
        // 复位
        ControlType.CONTROL_RESET -> {
            sendProtocol {
                control = 0x00
                data = tx.byteList.toByteArray()
            }
        }

        // 运动
        ControlType.CONTROL_MOVE -> {
            when (tx.executeType) {
                // 同步运动
                ExecuteType.SYNC -> {
                    try {
                        // 设置超时时间
                        withTimeout(tx.timeout) {
                            // 发送运动命令
                            if (tx.byteList.isNotEmpty()) {
                                setLock(tx.indexList)
                                sendProtocol {
                                    control = 0x01
                                    data = tx.byteList.toByteArray()
                                }
                                delay(10L)
                                // 等待运动完成
                                while (getLock(tx.indexList)) {
                                    delay(10L)
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        // 根据异常处理策略进行处理
                        when (tx.exceptionPolicy) {
                            // 重试
                            ExceptionPolicy.RETRY -> serial(block)
                            // 查询轴状态
                            ExceptionPolicy.QUERY -> serial { queryAxis(tx.indexList) }
                            // 跳过
                            ExceptionPolicy.SKIP -> setLock(tx.indexList, false)
                            // 复位
                            ExceptionPolicy.RESET -> serial { reset() }
                            // 抛出异常
                            ExceptionPolicy.THROW -> throw ex
                        }
                    }
                }

                // 异步运动
                ExecuteType.ASYNC -> {
                    if (tx.byteList.isNotEmpty()) {
                        setLock(tx.indexList)
                        sendProtocol {
                            control = 0x01
                            data = tx.byteList.toByteArray()
                        }
                    }
                }
            }
        }

        // 停止
        ControlType.CONTROL_STOP -> {
            if (tx.byteList.isNotEmpty()) {
                sendProtocol {
                    control = 0x02
                    data = tx.byteList.toByteArray()
                }
            }
        }

        // 查询轴状态
        ControlType.CONTROL_QUERY_AXIS -> {
            sendProtocol {
                control = 0x03
                data = tx.byteList.toByteArray()
            }
        }

        // 查询GPIO状态
        ControlType.CONTROL_QUERY_GPIO -> {
            sendProtocol {
                control = 0x04
                data = tx.byteList.toByteArray()
            }
        }

        // 控制气阀
        ControlType.CONTROL_VALVE -> {
            sendProtocol {
                control = 0x05
                data = tx.byteList.toByteArray()
            }
        }

        // 制胶
        ControlType.CONTROL_GLUE -> {
            // 发送运动命令
            if (tx.byteList.isNotEmpty()) {
                setLock(tx.indexList)
                sendProtocol {
                    control = 0x06
                    data = tx.byteList.toByteArray()
                }
                delay(10L)
                // 等待运动完成
                while (getLock(tx.indexList)) {
                    delay(10L)
                }
            }
        }
    }

    // 延迟
    delay(tx.delay)
}