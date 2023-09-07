package com.zktony.android.utils.extra

import com.zktony.serialport.AbstractSerialHelper
import com.zktony.serialport.command.Protocol
import com.zktony.serialport.command.modbus.RtuProtocol
import com.zktony.serialport.command.runze.RunzeProtocol
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import java.lang.Math.random

/**
 * 串口通信
 */
val serialPV = object : AbstractSerialHelper(SerialConfig(device = "/dev/ttyS3")) {
    override fun callbackHandler(byteArray: ByteArray) {
        if (byteArray[0] == 0xCC.toByte()) {
            RunzeProtocol.Protocol.callbackHandler(byteArray) { code, rx ->
                when (code) {
                    RunzeProtocol.CHANNEL -> {
                        appState.hpv[rx.slaveAddr.toInt()] = rx.data[0].toInt()
                    }

                    else -> {}
                }
            }
        } else {
            RtuProtocol.Protocol.callbackHandler(byteArray) { code, rx ->
                when (code) {
                    RtuProtocol.LOCATION -> {
                        val height = rx.data.copyOfRange(3, 5)
                        val low = rx.data.copyOfRange(1, 3)
                        appState.hpp[rx.slaveAddr.toInt() - 1] = height.plus(low).readInt32BE()
                    }

                    else -> {}
                }
            }
        }
    }
}

inline fun sendRunzeProtocol(block: RunzeProtocol.() -> Unit) =
    serialPV.sendByteArray(RunzeProtocol().apply(block).toByteArray())

inline fun sendRtuProtocol(block: RtuProtocol.() -> Unit) =
    serialPV.sendByteArray(RtuProtocol().apply(block).toByteArray())

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