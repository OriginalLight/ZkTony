package com.zktony.android.utils.extra

import com.zktony.android.data.entities.Motor
import com.zktony.android.utils.extra.internal.AppStateObserver
import com.zktony.android.utils.extra.internal.ExceptionPolicy
import com.zktony.android.utils.extra.internal.ExecuteType
import com.zktony.android.utils.extra.internal.SerialExtension
import com.zktony.serialport.AbstractSerialHelper
import com.zktony.serialport.command.Protocol
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.readInt8
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

val x: AtomicLong = AtomicLong(0L)
val y: AtomicLong = AtomicLong(0L)

/**
 * 轴状态
 */
val hpa: MutableMap<Int, Boolean> = ConcurrentHashMap<Int, Boolean>().apply {
    repeat(16) { put(it, false) }
}

/**
 * GPIO 状态
 */
val hpg: MutableMap<Int, Boolean> = ConcurrentHashMap<Int, Boolean>().apply {
    repeat(16) { put(it, false) }
}

/**
 * 电机信息
 */
val hpm: MutableMap<Int, Motor> = ConcurrentHashMap<Int, Motor>().apply {
    repeat(16) { put(it, Motor()) }
}

/**
 * 校准信息
 */
val hpc: MutableMap<Int, Double> = ConcurrentHashMap<Int, Double>().apply {
    put(0, 4.0 / 3200)
    put(1, 6.35 / 3200)
    repeat(14) { put(it + 2, 0.01) }
}

/**
 * 串口通信
 */
val serialport = object : AbstractSerialHelper(SerialConfig()) {

    val protocol = Protocol()

    override fun callbackHandler(byteArray: ByteArray) {
        protocol.callbackHandler(byteArray) { code, rx ->
            when (code) {
                Protocol.AXIS -> {
                    for (i in 0 until rx.data.size / 2) {
                        val index = rx.data.readInt8(offset = i * 2)
                        val status = rx.data.readInt8(offset = i * 2 + 1)
                        hpa[index] = status == 1
                    }
                }

                Protocol.GPIO -> {
                    for (i in 0 until rx.data.size / 2) {
                        val index = rx.data.readInt8(offset = i * 2)
                        val status = rx.data.readInt8(offset = i * 2 + 1)
                        hpg[index] = status == 1
                    }
                }
            }
        }
    }
}

/**
 * 应用状态观察者
 */
val observer = object : AppStateObserver() {
    override fun callbackOne(list: List<Motor>) {
        list.forEach {
            hpm[it.index] = it
        }
    }

    override fun callbackTwo(list: List<Double>) {
        list.forEachIndexed { index, d ->
            hpc[index + 2] = d
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
        is Double -> (dvp / hpc[index]!!).toLong()
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
fun setLock(ids: List<Int>, isLock: Boolean = true) = ids.forEach { hpa[it] = isLock }

/**
 * 设置轴锁定状态
 *
 * @param ids IntArray
 * @param isLock Boolean
 * @return Unit
 */
fun setLock(vararg ids: Int, isLock: Boolean = true) = ids.forEach { hpa[it] = isLock }

/**
 * 获取轴锁定状态
 *
 * @param ids List<Int>
 * @return Boolean
 */
fun getLock(ids: List<Int>) = ids.any { hpa[it] ?: false }

/**
 * 获取轴锁定状态
 *
 * @param ids IntArray
 * @return Boolean
 */
fun getLock(vararg ids: Int) = ids.any { hpa[it] ?: false }

/**
 * 获取 GPIO 状态
 *
 * @param id Int
 * @return Boolean
 */
fun getGpio(id: Int): Boolean = hpg[id] ?: false

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

        0x06.toByte() -> {
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