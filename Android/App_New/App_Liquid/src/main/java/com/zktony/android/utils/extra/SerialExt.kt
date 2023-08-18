package com.zktony.android.utils.extra

import com.zktony.android.utils.AsyncHelper
import com.zktony.android.utils.SerialHelper
import com.zktony.android.utils.model.ExceptionPolicy
import com.zktony.android.utils.model.ExecuteType
import com.zktony.android.utils.model.SerialConfig
import com.zktony.serialport.command.Protocol
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicLong

val serialHelper: SerialHelper = SerialHelper.instance
val asyncHelper: AsyncHelper = AsyncHelper.instance

val x: AtomicLong = AtomicLong(0L)
val y: AtomicLong = AtomicLong(0L)

fun <T : Number> pulse(index: Int, dvp: T): Long {

    val p = when (dvp) {
        is Double -> (dvp / asyncHelper.hpc[index]!!).toLong()
        is Long -> dvp
        else -> dvp.toLong()
    }

    return when (index) {
        0 -> {
            val d = p - x.get()
            x.set(maxOf(p, 0))
            d
        }

        1 -> {
            val d = p - y.get()
            y.set(maxOf(p, 0))
            d
        }

        else -> p
    }
}

inline fun sendProtocol(block: Protocol.() -> Unit) =
    serialHelper.sendProtocol(Protocol().apply(block))

fun setLock(ids: List<Int>, isLock: Boolean = true) =
    ids.forEach { serialHelper.axis[it] = isLock }

fun setLock(vararg ids: Int, isLock: Boolean = true) =
    ids.forEach { serialHelper.axis[it] = isLock }

fun getLock(ids: List<Int>) = ids.any { serialHelper.axis[it] }
fun getLock(vararg ids: Int) = ids.any { serialHelper.axis[it] }
fun getGpio(ids: List<Int>) = ids.any { serialHelper.gpio[it] }
fun getGpio(vararg ids: Int) = ids.any { serialHelper.gpio[it] }
suspend fun serial(block: SerialConfig.() -> Unit) {
    // 构建命令
    val tx = SerialConfig().apply(block)

    when (tx.controlType) {
        0x01.toByte() -> {
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
                                    control = tx.controlType
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
                            ExceptionPolicy.QUERY -> serial { query(tx.indexList) }
                            // 跳过
                            ExceptionPolicy.SKIP -> setLock(tx.indexList, false)
                            // 复位
                            ExceptionPolicy.RESET -> serial { init() }
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
                            control = tx.controlType
                            data = tx.byteList.toByteArray()
                        }
                    }
                }
            }
        }

        else -> {
            if (tx.byteList.isNotEmpty()) {
                sendProtocol {
                    control = tx.controlType
                    data = tx.byteList.toByteArray()
                }
            }
        }
    }
}