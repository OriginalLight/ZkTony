package com.zktony.android.utils

import com.zktony.android.utils.AppStateUtils.hpp
import com.zktony.android.utils.AppStateUtils.hpt
import com.zktony.android.utils.AppStateUtils.hpv
import com.zktony.android.utils.LogUtils.logE
import com.zktony.serialport.AbstractSerialHelper
import com.zktony.serialport.command.Protocol
import com.zktony.serialport.command.modbus.RtuProtocol
import com.zktony.serialport.command.runze.RunzeProtocol
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.math.absoluteValue

object SerialPortUtils {

    /**
     * 串口通信
     */
    val serialRtu =
        object : AbstractSerialHelper(SerialConfig(device = "/dev/ttyS3", baudRate = 9600)) {
            override fun callbackHandler(byteArray: ByteArray) {
                if (byteArray[0] == 0xCC.toByte()) {
                    RunzeProtocol.Protocol.callbackHandler(byteArray) { code, rx ->
                        when (code) {
                            RunzeProtocol.CHANNEL -> {
                                hpv[rx.slaveAddr.toInt()] = rx.data[0].toInt()
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
                                hpp[rx.slaveAddr.toInt() - 1] = height.plus(low).readInt32BE()
                            }

                            else -> {}
                        }
                    }
                }
            }

            override fun exceptionHandler(e: Exception) {
                logE(message = "Serial Exception: ${e.message}")
            }
        }

    val serialZkty =
        object : AbstractSerialHelper(SerialConfig(baudRate = 57600)) {
            override fun callbackHandler(byteArray: ByteArray) {
                val ascii = byteArray.toAsciiString()
                val address = ascii.substring(ascii.length - 2, ascii.length - 1).toInt()
                val data = ascii.replace("TC1:TCACTUALTEMP=", "").split("@")[0].format()
                hpt[address] = data.toDoubleOrNull() ?: 0.0
            }

            override fun exceptionHandler(e: Exception) {
                logE(message = "Serial Exception: ${e.message}")
            }
        }

    inline fun sendRunzeProtocol(block: RunzeProtocol.() -> Unit) =
        serialRtu.sendByteArray(RunzeProtocol().apply(block).toByteArray())

    inline fun sendRtuProtocol(block: RtuProtocol.() -> Unit) =
        serialRtu.sendByteArray(RtuProtocol().apply(block).toByteArray())

    inline fun sendProtocol(block: Protocol.() -> Unit) =
        serialZkty.sendByteArray(Protocol().apply(block).toByteArray())


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

    /**
     * 读取脉冲数
     */
    fun readWithPosition(slaveAddr: Int) =
        readRegister(slaveAddr = slaveAddr, startAddr = 4, quantity = 2)

    /**
     * 设置阀门状态
     */
    suspend fun writeWithValve(slaveAddr: Int, channel: Int, retry: Int = 3) {
        val current = hpv[slaveAddr] ?: 0
        if (current == channel) return
        try {
            withTimeout((current - channel).absoluteValue * 1000L) {
                sendRunzeProtocol {
                    this.slaveAddr = slaveAddr.toByte()
                    funcCode = 0x44
                    data = byteArrayOf(channel.toByte(), 0x00)
                }

                while (hpv[slaveAddr] != channel) {
                    repeat(3) {
                        delay(100L)
                        if (hpv[slaveAddr] == channel) return@withTimeout
                    }
                    readWithValve(slaveAddr)
                }
            }
        } catch (ex: TimeoutCancellationException) {
            if (hpv[slaveAddr] != channel && retry > 0) {
                writeWithValve(slaveAddr, channel, retry - 1)
            }
        }
    }

    /**
     * 发送脉冲数
     */
    suspend fun writeWithPulse(slaveAddr: Int, value: Long, retry: Int = 3) {
        if (value == 0L) return
        val before = hpp[slaveAddr] ?: 0
        try {
            withTimeout(maxOf(value.absoluteValue / 6400L, 1) * 1000L) {
                val target = (before + value - 5).toInt()..(before + value + 5).toInt()
                writeRegister(startAddr = 222, slaveAddr = slaveAddr, value = value)
                while ((hpp[slaveAddr] ?: 0) !in target) {
                    repeat(3) {
                        delay(100L)
                        if ((hpp[slaveAddr] ?: 0) in target) return@withTimeout
                    }
                    readWithPosition(slaveAddr)
                }
            }
        } catch (ex: TimeoutCancellationException) {
            if ((hpp[slaveAddr] ?: 0) == before && retry > 0) {
                writeWithPulse(slaveAddr, value, retry - 1)
            }
        }
    }

    /**
     * 发送温度
     */
    fun writeWithTemperature(id: Int, value: Double) {
        serialZkty.sendAsciiString("TC1:TCADJUSTTEMP=${String.format("%.2f", value)}@$id\n")
    }

    /**
     * 读取温度
     */
    fun readWithTemperature(id: Int) {
        serialZkty.sendAsciiString("TC1:TCACTUALTEMP?@$id\n")
    }
}

