package com.zktony.android.logic.ext

import com.zktony.android.logic.SerialPort
import com.zktony.android.logic.data.entities.MotorEntity
import com.zktony.serialport.command.Protocol
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.koin.java.KoinJavaComponent.inject

val serialPort: SerialPort by inject(SerialPort::class.java)

/**
 * 发送命令
 */
fun sendByteArray(byteArray: ByteArray) {
    serialPort.sendByteArray(byteArray)
}

fun sendProtocol(block: Protocol.() -> Unit) {
    serialPort.sendProtocol(Protocol().apply(block))
}

fun sendHexString(hex: String) {
    serialPort.sendHexString(hex)
}

fun sendAsciiString(ascii: String) {
    serialPort.sendAsciiString(ascii)
}

/**
 * 组合类
 */
class DV {
    val byteList: MutableList<Byte> = mutableListOf()
    val indexList: MutableList<Int> = mutableListOf()

    fun m0(dv: Float, config: MotorEntity = scheduleTask.hpm[0]!!) {
        byteList.addAll(pwc(0, dv, config).toList())
        indexList.add(0)
    }

    fun m0(pulse: Long, config: MotorEntity = scheduleTask.hpm[0]!!) {
        byteList.addAll(pwc(0, pulse, config).toList())
        indexList.add(0)
    }

    fun m1(dv: Float, config: MotorEntity = scheduleTask.hpm[1]!!) {
        byteList.addAll(pwc(1, dv, config).toList())
        indexList.add(1)
    }

    fun m1(pulse: Long, config: MotorEntity = scheduleTask.hpm[1]!!) {
        byteList.addAll(pwc(1, pulse, config).toList())
        indexList.add(1)
    }

    fun m2(dv: Float, config: MotorEntity = scheduleTask.hpm[2]!!) {
        byteList.addAll(pwc(2, dv, config).toList())
        indexList.add(2)
    }

    fun m2(pulse: Long, config: MotorEntity = scheduleTask.hpm[2]!!) {
        byteList.addAll(pwc(2, pulse, config).toList())
        indexList.add(2)
    }

    fun m3(dv: Float, config: MotorEntity = scheduleTask.hpm[3]!!) {
        byteList.addAll(pwc(3, dv, config).toList())
        indexList.add(3)
    }

    fun m3(pulse: Long, config: MotorEntity = scheduleTask.hpm[3]!!) {
        byteList.addAll(pwc(3, pulse, config).toList())
        indexList.add(3)
    }

    fun m4(dv: Float, config: MotorEntity = scheduleTask.hpm[4]!!) {
        byteList.addAll(pwc(4, dv, config).toList())
        indexList.add(4)
    }

    fun m4(pulse: Long, config: MotorEntity = scheduleTask.hpm[4]!!) {
        byteList.addAll(pwc(4, pulse, config).toList())
        indexList.add(4)
    }

    fun m5(dv: Float, config: MotorEntity = scheduleTask.hpm[5]!!) {
        byteList.addAll(pwc(5, dv, config).toList())
        indexList.add(5)
    }

    fun m5(pulse: Long, config: MotorEntity = scheduleTask.hpm[5]!!) {
        byteList.addAll(pwc(5, pulse, config).toList())
        indexList.add(5)
    }

    fun m6(dv: Float, config: MotorEntity = scheduleTask.hpm[6]!!) {
        byteList.addAll(pwc(6, dv, config).toList())
        indexList.add(6)
    }

    fun m6(pulse: Long, config: MotorEntity = scheduleTask.hpm[6]!!) {
        byteList.addAll(pwc(6, pulse, config).toList())
        indexList.add(6)
    }

    fun m7(dv: Float, config: MotorEntity = scheduleTask.hpm[7]!!) {
        byteList.addAll(pwc(7, dv, config).toList())
        indexList.add(7)
    }

    fun m7(pulse: Long, config: MotorEntity = scheduleTask.hpm[7]!!) {
        byteList.addAll(pwc(7, pulse, config).toList())
        indexList.add(7)
    }

    fun m8(dv: Float, config: MotorEntity = scheduleTask.hpm[8]!!) {
        byteList.addAll(pwc(8, dv, config).toList())
        indexList.add(8)
    }

    fun m8(pulse: Long, config: MotorEntity = scheduleTask.hpm[8]!!) {
        byteList.addAll(pwc(8, pulse, config).toList())
        indexList.add(8)
    }

    fun m9(dv: Float, config: MotorEntity = scheduleTask.hpm[9]!!) {
        byteList.addAll(pwc(9, dv, config).toList())
        indexList.add(9)
    }

    fun m9(pulse: Long, config: MotorEntity = scheduleTask.hpm[9]!!) {
        byteList.addAll(pwc(9, pulse, config).toList())
        indexList.add(9)
    }

    fun m10(dv: Float, config: MotorEntity = scheduleTask.hpm[10]!!) {
        byteList.addAll(pwc(10, dv, config).toList())
        indexList.add(10)
    }

    fun m10(pulse: Long, config: MotorEntity = scheduleTask.hpm[10]!!) {
        byteList.addAll(pwc(10, pulse, config).toList())
        indexList.add(10)
    }

    fun m11(dv: Float, config: MotorEntity = scheduleTask.hpm[11]!!) {
        byteList.addAll(pwc(11, dv, config).toList())
        indexList.add(11)
    }

    fun m11(pulse: Long, config: MotorEntity = scheduleTask.hpm[11]!!) {
        byteList.addAll(pwc(11, pulse, config).toList())
        indexList.add(11)
    }

    fun m12(dv: Float, config: MotorEntity = scheduleTask.hpm[12]!!) {
        byteList.addAll(pwc(12, dv, config).toList())
        indexList.add(12)
    }

    fun m12(pulse: Long, config: MotorEntity = scheduleTask.hpm[12]!!) {
        byteList.addAll(pwc(12, pulse, config).toList())
        indexList.add(12)
    }

    fun m13(dv: Float, config: MotorEntity = scheduleTask.hpm[13]!!) {
        byteList.addAll(pwc(13, dv, config).toList())
        indexList.add(13)
    }

    fun m13(pulse: Long, config: MotorEntity = scheduleTask.hpm[13]!!) {
        byteList.addAll(pwc(13, pulse, config).toList())
        indexList.add(13)
    }

    fun m14(dv: Float, config: MotorEntity = scheduleTask.hpm[14]!!) {
        byteList.addAll(pwc(14, dv, config).toList())
        indexList.add(14)
    }

    fun m14(pulse: Long, config: MotorEntity = scheduleTask.hpm[14]!!) {
        byteList.addAll(pwc(14, pulse, config).toList())
        indexList.add(14)
    }

    fun m15(dv: Float, config: MotorEntity = scheduleTask.hpm[15]!!) {
        byteList.addAll(pwc(15, dv, config).toList())
        indexList.add(15)
    }

    fun m15(pulse: Long, config: MotorEntity = scheduleTask.hpm[15]!!) {
        byteList.addAll(pwc(15, pulse, config).toList())
        indexList.add(15)
    }
}

/**
 * 组合发送
 *
 * 同步发送 一发一收算作完成
 *
 * @param block Compose.() -> Unit
 */
suspend fun syncTransmit(timeOut: Long = 5000L, block: DV.() -> Unit) {
    val dv = DV().apply(block)
    try {
        setLock(dv.indexList)
        withTimeout(timeOut) {
            sendProtocol {
                data = dv.byteList.toByteArray()
            }
            delay(100L)
            while (getLock(dv.indexList)) {
                delay(100L)
            }
        }
    } catch (e: Exception) {
        freeLock(dv.indexList)

    }
}

/**
 * 组合发送
 *
 * 异步发送 不管之前是否完成
 *
 * @param block Compose.() -> Unit
 */
fun asyncTransmit(block: DV.() -> Unit) {
    val dv = DV().apply(block)
    sendProtocol {
        data = dv.byteList.toByteArray()
    }
}

/**
 * 设置锁
 * @param list List<Int>
 */
fun setLock(list: List<Int>) {
    list.forEach {
        serialPort.arrayList[it] = 1
    }
}

/**
 * 设置锁
 * @param list List<Int>
 */
fun setLock(vararg list: Int) {
    list.forEach {
        serialPort.arrayList[it] = 1
    }
}

/**
 * 获取锁
 *
 * @param list List<Int>
 * @return Boolean
 */
fun getLock(list: List<Int>): Boolean {
    return list.any {
        serialPort.arrayList[it] == 1
    }
}

/**
 * 获取锁
 *
 * @param list List<Int>
 * @return Boolean
 */
fun getLock(vararg list: Int): Boolean {
    return list.any {
        serialPort.arrayList[it] == 1
    }
}

/**
 * 释放锁
 *
 * @param list List<Int>
 */
fun freeLock(list: List<Int>) {
    list.forEach {
        serialPort.arrayList[it] = 0
    }
}

/**
 * 释放锁
 *
 * @param list List<Int>
 */
fun freeLock(vararg list: Int) {
    list.forEach {
        serialPort.arrayList[it] = 0
    }
}