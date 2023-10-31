package com.zktony.android.utils

import com.zktony.android.utils.AppStateUtils.hps
import com.zktony.android.utils.AppStateUtils.hpv
import com.zktony.serialport.abstractSerialHelperOf
import com.zktony.serialport.command.modbus.RtuProtocol
import com.zktony.serialport.command.runze.RunzeProtocol
import com.zktony.serialport.ext.readInt16BE
import com.zktony.serialport.ext.toAsciiString
import com.zktony.serialport.ext.writeInt16BE
import com.zktony.serialport.ext.writeInt32BE
import com.zktony.serialport.ext.writeInt8
import com.zktony.serialport.lifecycle.SerialResult
import com.zktony.serialport.lifecycle.SerialStoreUtils
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.math.absoluteValue

object SerialPortUtils {

    fun with() {
        // 初始化rtu串口
        SerialStoreUtils.put("rtu", abstractSerialHelperOf {
            device = "/dev/ttyS3"
            baudRate = 9600
            log = true
        })
        // 初始化zkty串口
        SerialStoreUtils.put("zkty", abstractSerialHelperOf {
            baudRate = 57600
        })
        // rtu串口全局回调
        SerialStoreUtils.get("rtu")?.callbackHandler = { bytes ->
            if (bytes[0] == 0xCC.toByte()) {
                RunzeProtocol.Protocol.callbackHandler(bytes) { code, rx ->
                    when (code) {
                        RunzeProtocol.RX_0x00 -> {
                            hpv[rx.slaveAddr.toInt()] = rx.data[0].toInt()
                        }

                        else -> {}
                    }
                }
            } else {
                RtuProtocol.Protocol.callbackHandler(bytes) { code, rx ->
                    when (code) {
                        RtuProtocol.READ -> {
                            hps[rx.slaveAddr.toInt() - 1] = rx.data.readInt16BE(1)
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    /**
     * 读取寄存器
     */
    suspend fun readRegister(slaveAddr: Int, startAddr: Int, quantity: Int) {

    }

    /**
     * 写入 16 位整数
     */
    suspend fun writeRegister(slaveAddr: Int, startAddr: Int, value: Int) {
        SerialStoreUtils.get("rtu")?.sendByteArray(bytes = RtuProtocol().apply {
            this.slaveAddr = (slaveAddr + 1).toByte()
            funcCode = 0x06
            data = ByteArray(4).writeInt16BE(startAddr).writeInt16BE(value, 2)
        }.toByteArray())
    }

    /**
     * 写入 32 位整数
     */
    suspend fun writeRegister(slaveAddr: Int, startAddr: Int, value: Long) {
        SerialStoreUtils.get("rtu")?.sendByteArray(bytes = RtuProtocol().apply {
            val byteArray = ByteArray(4).writeInt32BE(value)
            this.slaveAddr = (slaveAddr + 1).toByte()
            funcCode = 0x10
            data = ByteArray(5)
                .plus(byteArray.copyOfRange(2, 4))
                .plus(byteArray.copyOfRange(0, 2))
                .writeInt16BE(startAddr)
                .writeInt16BE(2, 2)
                .writeInt8(4, 4)
        }.toByteArray())
    }

    /**
     * 设置阀门状态
     */
    suspend fun writeWithValve(slaveAddr: Int, channel: Int, retry: Int = 3) {
        val current = hpv[slaveAddr] ?: 0
        if (current == channel) return
        try {
            withTimeout((current - channel).absoluteValue * 1000L) {
                // 切阀命令
                SerialStoreUtils.get("rtu")?.sendByteArray(bytes = RunzeProtocol().apply {
                    this.slaveAddr = slaveAddr.toByte()
                    funcCode = 0x44
                    data = byteArrayOf(channel.toByte(), 0x00)
                }.toByteArray())

                while (hpv[slaveAddr] != channel) {
                    // 减小反应时间
                    repeat(3) {
                        delay(100L)
                        if (hpv[slaveAddr] == channel) return@withTimeout
                    }
                    // 读取阀门状态
                    SerialStoreUtils.get("rtu")?.sendByteArray(bytes = RunzeProtocol().apply {
                        this.slaveAddr = slaveAddr.toByte()
                        funcCode = 0x3E
                        data = byteArrayOf(0x00, 0x00)
                    }.toByteArray())
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
        val before = hps[slaveAddr] ?: 0
        try {
            withTimeout(maxOf(value.absoluteValue / 32000L, 1) * 1000L + 1000L) {
                writeRegister(startAddr = 222, slaveAddr = slaveAddr, value = value)
                hps[slaveAddr] = 1
                while ((hps[slaveAddr] ?: 0) != 0) {
                    // 减小反应时间
                    repeat(3) {
                        delay(100L)
                        if ((hps[slaveAddr] ?: 0) == 0) return@withTimeout
                    }
                    // 读取当前的速度
                    SerialStoreUtils.get("rtu")?.sendByteArray(bytes = RtuProtocol().apply {
                        this.slaveAddr = (slaveAddr + 1).toByte()
                        funcCode = 0x03
                        data = ByteArray(4).writeInt16BE(25).writeInt16BE(1, 2)
                    }.toByteArray())
                }
            }
        } catch (ex: TimeoutCancellationException) {
            if ((hps[slaveAddr] ?: 0) == before && retry > 0) {
                writeWithPulse(slaveAddr, value, retry - 1)
            }
        }
    }

    /**
     * 发送温度
     */
    suspend fun writeWithTemperature(id: Int, value: Double) {
        SerialStoreUtils.get("zkty")
            ?.sendAsciiString("TC1:TCADJUSTTEMP=${String.format("%.2f", value)}@$id\n")
    }

    /**
     * 读取温度
     */
    suspend fun readWithTemperature(id: Int, block: (Int, Double) -> Unit) {
        SerialStoreUtils.get("zkty")?.sendAsciiString("TC1:TCACTUALTEMP?@$id\n") { res ->
            when (res) {
                is SerialResult.Success -> {
                    val ascii = res.byteArray.toAsciiString()
                    val address =
                        ascii.substring(ascii.length - 2, ascii.length - 1).toInt()
                    val data = ascii.replace("TC1:TCACTUALTEMP=", "").split("@")[0].format()
                    block(address, data.toDoubleOrNull() ?: 0.0)
                }

                else -> {}
            }
        }
    }
}