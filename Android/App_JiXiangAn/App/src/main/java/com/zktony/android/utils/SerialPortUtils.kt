package com.zktony.android.utils

import android.annotation.SuppressLint
import android.util.Log
import com.zktony.android.utils.AppStateUtils.hps
import com.zktony.android.utils.AppStateUtils.hpv
import com.zktony.serialport.command.modbus.RtuProtocol
import com.zktony.serialport.command.runze.RunzeProtocol
import com.zktony.serialport.ext.readInt16BE
import com.zktony.serialport.ext.toAsciiString
import com.zktony.serialport.ext.writeInt16BE
import com.zktony.serialport.ext.writeInt32BE
import com.zktony.serialport.ext.writeInt8
import com.zktony.serialport.lifecycle.SerialStoreUtils
import com.zktony.serialport.serialPortOf
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.math.absoluteValue

object SerialPortUtils {

    fun with() {
        // 初始化rtu串口
        serialPortOf {
            device = "/dev/ttyS2"
            baudRate = 115200
            log = true
        }?.let { SerialStoreUtils.put("rtu", it) }
        // 初始化tec串口
        serialPortOf {
            device = "/dev/ttyS0"
            baudRate = 57600
        }?.let { SerialStoreUtils.put("tec", it) }
        // rtu串口全局回调
//        SerialStoreUtils.get("rtu")?.registerCallback("globe") { bytes ->
//            if (bytes[0] == 0xCC.toByte()) {
//                RunzeProtocol.verifyProtocol(bytes) { protocol ->
//                    // 处理数据包
//                    when (protocol.funcCode) {
//                        0x00.toByte() -> {
//                            hpv[protocol.slaveAddr.toInt()] = protocol.data[0].toInt()
//                        }
//
//                        0x01.toByte() -> throw Exception("帧错误")
//                        0x02.toByte() -> throw Exception("参数错误")
//                        0x03.toByte() -> throw Exception("光耦错误")
//                        0x04.toByte() -> throw Exception("电机忙")
//                        0x05.toByte() -> throw Exception("电机堵转")
//                        0x06.toByte() -> throw Exception("未知位置")
//                        0xFE.toByte() -> throw Exception("任务挂起")
//                        0xFF.toByte() -> throw Exception("未知错误")
//                        else -> {}
//                    }
//                }
//            } else {
//                RtuProtocol.verifyProtocol(bytes) { protocol ->
//                    when (protocol.funcCode) {
//                        0x03.toByte() -> {
//                            hps[protocol.slaveAddr.toInt() - 1] = protocol.data.readInt16BE(1)
//                        }
//
//                        else -> {}
//                    }
//                }
//            }
//        }
    }

    /**
     * 写入 16 位整数
     */
    fun writeRegister(slaveAddr: Int, startAddr: Int, value: Int) {
        SerialStoreUtils.get("rtu")?.sendByteArray(bytes = RtuProtocol().apply {
            this.slaveAddr = (slaveAddr + 1).toByte()
            funcCode = 0x06
            data = ByteArray(4).writeInt16BE(startAddr).writeInt16BE(value, 2)
        }.serialization())
    }

    /**
     * 写入 32 位整数
     */
    fun writeRegister(slaveAddr: Int, startAddr: Int, value: Long) {
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
        }.serialization())
    }

    /**
     * 设置阀门状态
     */
    suspend fun writeWithValve(slaveAddr: Int, channel: Int, retry: Int = 1) {
        val current = hpv[slaveAddr] ?: 0
        if (current == channel) return
        val rtu = SerialStoreUtils.get("rtu") ?: error("ERROR 0X0000 - 串口未初始化")
        try {
            withTimeout((current - channel).absoluteValue * 1000L + 1000L) {
                // 切阀命令
                rtu.sendByteArray(bytes = RunzeProtocol().apply {
                    this.slaveAddr = slaveAddr.toByte()
                    funcCode = 0x44
                    data = byteArrayOf(channel.toByte(), 0x00)
                }.serialization())

                while (hpv[slaveAddr] != channel) {
                    // 减小反应时间
                    repeat(3) {
                        delay(100L)
                        if (hpv[slaveAddr] == channel) return@withTimeout
                    }
                    // 读取阀门状态
                    rtu.sendByteArray(bytes = RunzeProtocol().apply {
                        this.slaveAddr = slaveAddr.toByte()
                        funcCode = 0x3E
                        data = byteArrayOf(0x00, 0x00)
                    }.serialization())
                }
            }
        } catch (ex: TimeoutCancellationException) {
            if (hpv[slaveAddr] != channel && retry > 0) {
                writeWithValve(slaveAddr, channel, retry - 1)
            } else {
                throw Exception("ERROR 0X0002 - 切阀超时")
            }
        }
    }

    /**
     * 发送脉冲数
     */
    suspend fun writeWithPulse(slaveAddr: Int, value: Long) {
        if (value == 0L) return
        val rtu = SerialStoreUtils.get("rtu") ?: error("ERROR 0X0000 - 串口未初始化")
        try {
            withTimeout(maxOf(value.absoluteValue / 32000L, 1) * 1000L + 2000L) {
                writeRegister(startAddr = 222, slaveAddr = slaveAddr, value = value)
                hps[slaveAddr] = 1
                while ((hps[slaveAddr] ?: 0) != 0) {
                    // 减小反应时间
                    repeat(3) {
                        delay(50L)
                        if ((hps[slaveAddr] ?: 0) == 0) return@withTimeout
                    }
                    // 读取当前的速度
                    rtu.sendByteArray(bytes = RtuProtocol().apply {
                        this.slaveAddr = (slaveAddr + 1).toByte()
                        funcCode = 0x03
                        data = ByteArray(4).writeInt16BE(25).writeInt16BE(1, 2)
                    }.serialization())
                }
            }
        } catch (ex: TimeoutCancellationException) {
            writeRegister(slaveAddr = slaveAddr, startAddr = 200, value = 0)
            error("ERROR 0X0001 - 电机运行超时")
        }
    }

    /**
     * 发送温度
     */
    @SuppressLint("DefaultLocale")
    suspend fun writeWithTemperature(id: Int, value: Double) {
        val tec = SerialStoreUtils.get("tec") ?: return
        tec.sendAsciiString("TC1:TCSW=0@$id\r")
        delay(15 * 1000L)
        tec.sendAsciiString("TC1:TCADJUSTTEMP=${String.format("%.2f", value)}@$id\r")
        delay(15 * 1000L)
        tec.sendAsciiString("TC1:TCSW=1@$id\r")
    }

    /**
     * 读取温度
     */
    suspend fun readWithTemperature(id: Int, block: (Int, Double) -> Unit) {
        var rx = 0
        val key = "readWithTemperature"
        val tec = SerialStoreUtils.get("tec") ?: return
        try {
            tec.registerCallback(key) { res ->
                rx += 1
                val ascii = res.toAsciiString()
                val address = ascii.substring(ascii.length - 2, ascii.length - 1).toInt()
                val data = ascii.replace("TC1:TCACTUALTEMP=", "").split("@")[0].format()
                block(address, data.toDoubleOrNull() ?: 0.0)
            }
            withTimeout(1000L) {
                tec.sendAsciiString("TC1:TCACTUALTEMP?@$id\r")
                while (rx == 0) {
                    delay(100L)
                }
            }
        } catch (ex: Exception) {
            Log.e("SerialPortUtils", ex.message ?: "Unknown")
        } finally {
            tec.unregisterCallback(key)
        }
    }
}