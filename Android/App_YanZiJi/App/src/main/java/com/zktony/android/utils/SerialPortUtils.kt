package com.zktony.android.utils

import com.zktony.android.utils.AppStateUtils.hps
import com.zktony.android.utils.AppStateUtils.hpv
import com.zktony.log.LogUtils
import com.zktony.serialport.command.modbus.RtuProtocol
import com.zktony.serialport.command.runze.RunzeProtocol
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
            log = true
        }?.let {
            SerialStoreUtils.put("A", it)
            LogUtils.info("串口-A-下位机-ttyS2-初始化完成", true)
        } ?: {
            LogUtils.error("串口-A-下位机-ttyS2-初始化化失败", true)
        }
        // 初始化tec串口
        serialPortOf {
            device = "/dev/ttyS0"
            log = true
        }?.let {
            SerialStoreUtils.put("B", it)
            LogUtils.info("串口-B-下位机-ttyS0-初始化完成", true)
        } ?: {
            LogUtils.error("串口-B-下位机-ttyS0-初始化失败", true)
        }
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

}