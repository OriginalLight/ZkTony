package com.zktony.android.utils.extra

import com.zktony.serialport.AbstractSerialHelper
import com.zktony.serialport.command.modbus.RtuProtocol
import com.zktony.serialport.command.modbus.toRtuProtocol
import com.zktony.serialport.command.runze.RunzeProtocol
import com.zktony.serialport.command.runze.toRunzeProtocol
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

/**
 * 串口通信
 */
val serialHelper = object : AbstractSerialHelper(SerialConfig(device = "/dev/ttyS3")) {
    override fun callbackVerify(byteArray: ByteArray, block: (ByteArray) -> Unit) {
        byteArray.toHexString().loge()
        if (byteArray[0] == 0xCC.toByte()) {
            // checksum 校验
            val crc = byteArray.copyOfRange(byteArray.size - 2, byteArray.size)
            val bytes = byteArray.copyOfRange(0, byteArray.size - 2)
            if (!bytes.checkSumLE().contentEquals(crc)) {
                throw Exception("RX Crc Error with byteArray: ${byteArray.toHexString()}")
            }
        } else {
            // crc 校验
            val crc = byteArray.copyOfRange(byteArray.size - 2, byteArray.size)
            val bytes = byteArray.copyOfRange(0, byteArray.size - 2)
            if (!bytes.crc16LE().contentEquals(crc)) {
                throw Exception("RX Crc Error with byteArray: ${byteArray.toHexString()}")
            }
        }

        // 校验通过
        block(byteArray)
    }

    override fun callbackProcess(byteArray: ByteArray) {
        // 解析协议
        if (byteArray[0] == 0xCC.toByte()) {
            val rx = byteArray.toRunzeProtocol()
            when (rx.funcCode) {
                0x00.toByte() -> {
                    appState.hpv[rx.slaveAddr.toInt()] = rx.data[0].toInt()
                }

                0x01.toByte() -> throw Exception("帧错误")
                0x02.toByte() -> throw Exception("参数错误")
                0x03.toByte() -> throw Exception("光耦错误")
                0x04.toByte() -> throw Exception("电机忙")
                0x05.toByte() -> throw Exception("电机堵转")
                0x06.toByte() -> throw Exception("未知位置")
                0xFE.toByte() -> throw Exception("任务挂起")
                0xFF.toByte() -> throw Exception("未知错误")
                else -> {}
            }
        } else {
            val rx = byteArray.toRtuProtocol()
            when (rx.funcCode) {
                0x03.toByte() -> {
                    val height = rx.data.copyOfRange(3, 5)
                    val low = rx.data.copyOfRange(1, 3)
                    appState.hpp[rx.slaveAddr.toInt() - 1] = height.plus(low).readInt32BE()
                }

                else -> {}
            }
        }
    }
}

inline fun sendRunzeProtocol(block: RunzeProtocol.() -> Unit) =
    serialHelper.sendByteArray(RunzeProtocol().apply(block).toByteArray())

inline fun sendRtuProtocol(block: RtuProtocol.() -> Unit) =
    serialHelper.sendByteArray(RtuProtocol().apply(block).toByteArray())

/**
 * 读取寄存器
 */
fun readRegister(slaveAddr: Int, startAddr: Int, quantity: Int) =
    sendRtuProtocol {
        this.slaveAddr = (slaveAddr + 1).toByte()
        funcCode = 0x03
        data = ByteArray(4).writeInt16BE(startAddr).writeInt16BE(quantity, 2)
    }

/**
 * 写入 16 位整数
 */
fun writeRegister(slaveAddr: Int, startAddr: Int, value: Int) =
    sendRtuProtocol {
        this.slaveAddr = (slaveAddr + 1).toByte()
        funcCode = 0x06
        data = ByteArray(4).writeInt16BE(startAddr).writeInt16BE(value, 2)
    }

/**
 * 写入 32 位整数
 */
fun writeRegister(slaveAddr: Int, startAddr: Int, value: Long) =
    sendRtuProtocol {
        val byteArray = ByteArray(4).writeInt32BE(value)
        this.slaveAddr = (slaveAddr + 1).toByte()
        funcCode = 0x10
        data = ByteArray(5)
            .plus(byteArray.copyOfRange(2, 4))
            .plus(byteArray.copyOfRange(0, 2))
            .writeInt16BE(startAddr)
            .writeInt16BE(2, 2)
            .writeInt8(4, 4)
    }

/**
 * 读取阀门状态
 */
fun readWithValve(slaveAddr: Int) =
    sendRunzeProtocol {
        this.slaveAddr = slaveAddr.toByte()
        funcCode = 0x3E
        data = byteArrayOf(0x00, 0x00)
    }

fun readWithPulse(slaveAddr: Int) =
    readRegister(slaveAddr = slaveAddr, startAddr = 4, quantity = 2)

/**
 * 设置阀门状态
 */
@Throws(Exception::class)
suspend fun writeWithValve(slaveAddr: Int, channel: Int, timeOut: Long = 1000L * 10) {
    withTimeout(timeOut) {
        appState.hpv[slaveAddr] = 0
        sendRunzeProtocol {
            this.slaveAddr = slaveAddr.toByte()
            funcCode = 0x44
            data = byteArrayOf(channel.toByte(), 0x00)
        }
        while (appState.hpv[slaveAddr] != channel) {
            delay(200L)
            readWithValve(slaveAddr)
        }
    }
}

/**
 * 发送脉冲数
 */
@Throws(Exception::class)
suspend fun writeWithPulse(slaveAddr: Int, value: Long, timeOut: Long = 1000L * 10) {
    if (value == 0L) throw Exception("value must be greater than 0")
    withTimeout(timeOut) {
        val startPosition = appState.hpp[slaveAddr] ?: 0
        writeRegister(startAddr = 222, slaveAddr = slaveAddr, value = value)
        while (appState.hpp[slaveAddr] != startPosition + value.toInt()) {
            delay(200L)
            readRegister(slaveAddr = slaveAddr, startAddr = 4, quantity = 2)
        }
    }
}

/**
 * 电机一直转
 * 0 - 减速停止
 * 1 - 正转
 * 256 - 急停
 * 257 - 反转
 */
@Throws(Exception::class)
fun writeWithSwitch(slaveAddr: Int, value: Int) {
    // 检测value 是否合法
    if (value !in listOf(0, 1, 256, 257)) throw Exception("value must be 0, 1, 256, 257")
    writeRegister(slaveAddr = slaveAddr, startAddr = 200, value = value)
}

/**
 * 运行到指定位置
 */
@Throws(Exception::class)
suspend fun writeWithPosition(slaveAddr: Int, value: Long, timeOut: Long = 1000L * 10) {
    withTimeout(timeOut) {
        writeRegister(slaveAddr = slaveAddr, startAddr = 208, value = value)
        while (appState.hpp[slaveAddr] != value.toInt()) {
            delay(200L)
            readRegister(slaveAddr = slaveAddr, startAddr = 4, quantity = 2)
        }
    }
}