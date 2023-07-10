package com.zktony.android.core.dsl

import com.zktony.android.core.ScheduleTask
import com.zktony.android.core.SerialPort
import com.zktony.android.core.utils.Constants
import com.zktony.android.core.utils.ControlType
import com.zktony.android.core.utils.ExceptionPolicy
import com.zktony.android.core.utils.ExecuteType
import com.zktony.serialport.command.Protocol
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.atomic.AtomicLong

val serialPort: SerialPort by inject(SerialPort::class.java)
val scheduleTask: ScheduleTask by inject(ScheduleTask::class.java)


private var y: AtomicLong = AtomicLong(0L)
private var z: AtomicLong = AtomicLong(0L)

/**
 * 脉冲
 *
 * @param index Int
 * @param dvp T
 * @return T
 */
fun <T : Number> pulse(index: Int, dvp: T): Long {

    val p = when (dvp) {
        is Float -> {
            (dvp / scheduleTask.hpc[index]!!).toLong()
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

/**
 * 发送命令
 *
 * @param byteArray ByteArray
 * @return Unit
 */
fun sendByteArray(byteArray: ByteArray) {
    serialPort.sendByteArray(byteArray)
}

/**
 * 发送命令
 *
 * @param block [@kotlin.ExtensionFunctionType] Function1<Protocol, Unit>
 * @return Unit
 */
fun sendProtocol(block: Protocol.() -> Unit) {
    serialPort.sendProtocol(Protocol().apply(block))
}

/**
 * 发送命令
 *
 * @param hex String
 * @return Unit
 */
fun sendHexString(hex: String) {
    serialPort.sendHexString(hex)
}

/**
 * 发送命令
 *
 * @param ascii String
 * @return Unit
 */
fun sendAsciiString(ascii: String) {
    serialPort.sendAsciiString(ascii)
}

/**
 * 设置电机状态
 *
 * @param ids List<Int>
 * @return Unit
 */
fun setLock(ids: List<Int>, isLock: Boolean = true) {
    ids.forEach {
        serialPort.axis[it] = isLock
    }
}

/**
 * 设置电机状态
 *
 * @param ids IntArray
 * @return Unit
 */
fun setLock(vararg ids: Int, isLock: Boolean = true) {
    ids.forEach {
        serialPort.axis[it] = isLock
    }
}

/**
 * 获取电机状态
 *
 * @param ids List<Int>
 * @return Boolean
 */
fun getLock(ids: List<Int>): Boolean {
    return ids.any {
        serialPort.axis[it] == true
    }
}

/**
 * 获取电机状态
 *
 * @param ids List<Int>
 * @return Boolean
 */
fun getLock(vararg ids: Int): Boolean {
    return ids.any { serialPort.axis[it] }
}

/**
 * 获取光电状态
 *
 * @param ids List<Int>
 * @return Boolean
 */
fun getGpio(ids: List<Int>): Boolean {
    return ids.any {
        serialPort.gpio[it] == true
    }
}

/**
 * 获取光电状态
 *
 * @param ids List<Int>
 * @return Boolean
 */
fun getGpio(vararg ids: Int): Boolean {
    return ids.any { serialPort.gpio[it] }
}

/**
 * 发送命令
 *
 * @param block [TxDsl.() -> Unit] 命令构建器
 * @return [Unit]
 */
suspend fun tx(block: TxDsl.() -> Unit): Unit {
    // 构建命令
    val tx = TxDsl().apply(block)

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
                            ExceptionPolicy.RETRY -> tx(block)
                            // 查询轴状态
                            ExceptionPolicy.QUERY -> tx { queryAxis(tx.indexList) }
                            // 跳过
                            ExceptionPolicy.SKIP -> setLock(tx.indexList, false)
                            // 复位
                            ExceptionPolicy.RESET -> tx { reset() }
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
            sendProtocol {
                control = 0x02
                data = tx.byteList.toByteArray()
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
    }

    // 延迟
    delay(tx.delay)
}

/**
 * 电机初始化
 *
 * @param ids IntArray 电机ID列表
 * @return Unit
 */
suspend fun axisInitializer(vararg ids: Int): Unit {
    // 查询GPIO状态
    tx {
        delay = 300L
        queryGpio(ids.toList())
    }

    // 针对每个电机进行初始化
    ids.forEach {
        // 如果电机未初始化，则进行初始化
        if (!getGpio(it)) {
            // 进行电机初始化
            tx {
                timeout = 1000L * 60
                mpm {
                    index = it
                    pulse = 3200L * -30
                    acc = 100f
                    dec = 150f
                    speed = 200f
                }
            }
        }

        // 进行正向运动
        tx {
            timeout = 1000L * 10
            mpm {
                index = it
                pulse = 3200L * 2
                acc = 50f
                dec = 80f
                speed = 100f
            }
        }

        // 进行反向运动
        tx {
            timeout = 1000L * 15
            mpm {
                index = it
                pulse = 3200L * -3
                acc = 50f
                dec = 80f
                speed = 100f
            }
        }
    }
}

/**
 * 注射泵初始化
 *
 * @param ids List<Int> 注射泵ID列表
 * @return Unit
 */
suspend fun syringeInitializer(vararg ids: Int) {
    // 查询GPIO状态
    tx { queryGpio(ids.toList()) }

    // 延迟
    delay(100L)

    // 关闭所有气阀
    tx { valve(ids.toList().map { it to 0 }) }

    // 对每个注射泵进行初始化
    tx {
        timeout = 1000L * 60
        ids.forEach {
            // 进行注射泵初始化
            mpm {
                index = it
                pulse = Constants.MAX_SYRINGE * -1
                acc = 300f
                dec = 400f
                speed = 600f
            }
        }
    }
}