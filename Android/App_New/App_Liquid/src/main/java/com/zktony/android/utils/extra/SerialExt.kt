package com.zktony.android.utils.extra

import com.zktony.android.utils.extra.internal.ExceptionPolicy
import com.zktony.android.utils.extra.internal.ExecuteType
import com.zktony.android.utils.extra.internal.SerialExtension
import com.zktony.serialport.AbstractSerialHelper
import com.zktony.serialport.command.Protocol
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.readInt8
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

/**
 * 串口通信
 */
val serialport = object : AbstractSerialHelper(SerialConfig()) {

    override fun callbackHandler(byteArray: ByteArray) {
        Protocol.Protocol.callbackHandler(byteArray) { code, rx ->
            when (code) {
                Protocol.AXIS -> {
                    for (i in 0 until rx.data.size / 2) {
                        val index = rx.data.readInt8(offset = i * 2)
                        val status = rx.data.readInt8(offset = i * 2 + 1)
                        appState.hpa[index] = status == 1
                    }
                }

                Protocol.GPIO -> {
                    for (i in 0 until rx.data.size / 2) {
                        val index = rx.data.readInt8(offset = i * 2)
                        val status = rx.data.readInt8(offset = i * 2 + 1)
                        appState.hpg[index] = status == 1
                    }
                }
            }
        }
    }
}

/**
 * 脉冲转换
 *
 * @param index Int
 * @param dvp T
 * @return Long
 */
fun <T : Number> pulse(index: Int, dvp: T): Long {
    val p = when (dvp) {
        is Double -> ((appState.hpc[index] ?: { x -> x * 100 }).invoke(dvp) ?: 0.0).toLong()
        is Long -> dvp
        else -> dvp.toLong()
    }

    return if (index in 0..1) {
        val d = p - (appState.hpp[index] ?: 0L)
        appState.hpp[index] = maxOf(p, 0L)
        d
    } else {
        p
    }
}

/**
 * 发送协议
 *
 * @param block [@kotlin.ExtensionFunctionType] Function1<Protocol, Unit>
 * @return Unit
 */
inline fun sendProtocol(block: Protocol.() -> Unit) =
    serialport.sendByteArray(Protocol().apply(block).toByteArray())

/**
 * 设置轴锁定状态
 *
 * @param ids List<Int>
 * @param isLock Boolean
 * @return Unit
 */
fun setLock(ids: List<Int>, isLock: Boolean = true) = ids.forEach { appState.hpa[it] = isLock }

/**
 * 设置轴锁定状态
 *
 * @param ids IntArray
 * @param isLock Boolean
 * @return Unit
 */
fun setLock(vararg ids: Int, isLock: Boolean = true) = ids.forEach { appState.hpa[it] = isLock }

/**
 * 获取轴锁定状态
 *
 * @param ids List<Int>
 * @return Boolean
 */
fun getLock(ids: List<Int>) = ids.any { appState.hpa[it] ?: false }

/**
 * 获取轴锁定状态
 *
 * @param ids IntArray
 * @return Boolean
 */
fun getLock(vararg ids: Int) = ids.any { appState.hpa[it] ?: false }

/**
 * 获取 GPIO 状态
 *
 * @param id Int
 * @return Boolean
 */
fun getGpio(id: Int): Boolean = appState.hpg[id] ?: false

/**
 * 串口通信
 *
 * @param block [@kotlin.ExtensionFunctionType] Function1<SerialExtension, Unit>
 * @return Unit
 */
suspend fun serial(block: SerialExtension.() -> Unit) {
    // 构建命令
    val ext = SerialExtension().apply(block)

    when (ext.controlType) {
        0x01.toByte() -> {
            when (ext.executeType) {
                // 同步运动
                ExecuteType.SYNC -> {
                    try {
                        // 设置超时时间
                        withTimeout(ext.timeout) {
                            // 发送运动命令
                            if (ext.byteList.isNotEmpty()) {
                                setLock(ext.indexList)
                                sendProtocol {
                                    func = ext.controlType
                                    data = ext.byteList.toByteArray()
                                }
                                delay(10L)
                                // 等待运动完成
                                while (getLock(ext.indexList)) {
                                    delay(10L)
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        // 根据异常处理策略进行处理
                        when (ext.exceptionPolicy) {
                            // 重试
                            ExceptionPolicy.RETRY -> serial(block)
                            // 查询轴状态
                            ExceptionPolicy.QUERY -> serial { query(ext.indexList) }
                            // 跳过
                            ExceptionPolicy.SKIP -> setLock(ext.indexList, false)
                            // 复位
                            ExceptionPolicy.RESET -> serial { init() }
                            // 抛出异常
                            ExceptionPolicy.THROW -> throw ex
                        }
                    }
                }

                // 异步运动
                ExecuteType.ASYNC -> {
                    if (ext.byteList.isNotEmpty()) {
                        setLock(ext.indexList)
                        sendProtocol {
                            func = ext.controlType
                            data = ext.byteList.toByteArray()
                        }
                    }
                }
            }
        }

        else -> {
            if (ext.byteList.isNotEmpty()) {
                sendProtocol {
                    func = ext.controlType
                    data = ext.byteList.toByteArray()
                }
            }
        }
    }
}