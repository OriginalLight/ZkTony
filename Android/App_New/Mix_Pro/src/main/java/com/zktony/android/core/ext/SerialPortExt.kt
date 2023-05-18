package com.zktony.android.core.ext

import com.zktony.android.core.SerialPort
import com.zktony.android.data.entity.MotorEntity
import com.zktony.serialport.command.Protocol
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.koin.java.KoinJavaComponent.inject

val serialPort: SerialPort by inject(SerialPort::class.java)

/**
 * 发送命令
 */
fun sendByteArray(block: Protocol.() -> Unit) {
    serialPort.sendByteArray(Protocol().apply(block).toByteArray())
}

/**
 * 组合类
 *
 * @property list MutableList<Triple<Int, Float, MotorEntity>>
 */
class Compose {
    val list = mutableListOf<Triple<Int, Float, MotorEntity>>()

    fun x(dv: Float, config: MotorEntity = scheduleTask.hpm[0]!!) {
        list.add(Triple(0, dv, config))
    }

    fun y(dv: Float, config: MotorEntity = scheduleTask.hpm[1]!!) {
        list.add(Triple(1, dv, config))
    }

    fun z(dv: Float, config: MotorEntity = scheduleTask.hpm[2]!!) {
        list.add(Triple(2, dv, config))
    }

    fun v1(dv: Float, config: MotorEntity = scheduleTask.hpm[3]!!) {
        list.add(Triple(3, dv, config))
    }

    fun v2(dv: Float, config: MotorEntity = scheduleTask.hpm[4]!!) {
        list.add(Triple(4, dv, config))
    }

    fun v3(dv: Float, config: MotorEntity = scheduleTask.hpm[5]!!) {
        list.add(Triple(5, dv, config))
    }

    fun v4(dv: Float, config: MotorEntity = scheduleTask.hpm[6]!!) {
        list.add(Triple(6, dv, config))
    }

    fun v5(dv: Float, config: MotorEntity = scheduleTask.hpm[7]!!) {
        list.add(Triple(7, dv, config))
    }

    fun v6(dv: Float, config: MotorEntity = scheduleTask.hpm[8]!!) {
        list.add(Triple(8, dv, config))
    }

    fun v7(dv: Float, config: MotorEntity = scheduleTask.hpm[9]!!) {
        list.add(Triple(9, dv, config))
    }

    fun v8(dv: Float, config: MotorEntity = scheduleTask.hpm[10]!!) {
        list.add(Triple(10, dv, config))
    }

    fun v9(dv: Float, config: MotorEntity = scheduleTask.hpm[11]!!) {
        list.add(Triple(11, dv, config))
    }

    fun v10(dv: Float, config: MotorEntity = scheduleTask.hpm[12]!!) {
        list.add(Triple(12, dv, config))
    }

    fun v11(dv: Float, config: MotorEntity = scheduleTask.hpm[13]!!) {
        list.add(Triple(13, dv, config))
    }

    fun v12(dv: Float, config: MotorEntity = scheduleTask.hpm[14]!!) {
        list.add(Triple(14, dv, config))
    }

    fun v13(dv: Float, config: MotorEntity = scheduleTask.hpm[15]!!) {
        list.add(Triple(15, dv, config))
    }
}

/**
 * 组合发送
 *
 * 同步发送 一发一收算作完成
 *
 * @param block Compose.() -> Unit
 */
suspend fun syncHex(block: Compose.() -> Unit, timeOut: Long = 5000) {
    val compose = Compose().apply(block)
    val list = compose.list
    val bytes = byteArrayOf()
    list.forEach {
        bytes.plus(pwc(it.first, it.second, it.third))
    }
    try {
        withTimeout(timeOut) {
            sendByteArray { data = bytes }
            delay(100L)
            while (getLock(list.map { it.first })) {
                delay(100L)
            }
        }
    } catch (e: Exception) {
        freeLock(list.map { it.first })
    }
}

/**
 * 组合发送
 *
 * 异步发送 不管之前是否完成
 *
 * @param block Compose.() -> Unit
 */
fun asyncHex(block: Compose.() -> Unit) {
    val compose = Compose().apply(block)
    val list = compose.list
    val bytes = byteArrayOf()
    list.forEach {
        bytes.plus(pwc(it.first, it.second, it.third))
    }
    sendByteArray { data = bytes }
}

/**
 * 获取锁
 *
 * @param list List<Int>
 * @return Boolean
 */
fun getLock(list: List<Int>): Boolean {
    var lock = false
    list.forEach {
        lock = lock || serialPort.vector[it] != 0
    }
    return lock
}

/**
 * 释放锁
 *
 * @param list List<Int>
 */
fun freeLock(list: List<Int>) {
    list.forEach {
        serialPort.vector[it] = 0
    }
}

/**
 * 收集hex回复
 * @param block (String) -> Unit
 */
suspend fun collectHex(block: (ByteArray) -> Unit) {
    serialPort.callback.collect {
        block(it)
    }
}