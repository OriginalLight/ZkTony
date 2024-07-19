package com.zktony.android.utils

import com.zktony.android.data.Arguments
import com.zktony.android.data.ArgumentsBubble
import com.zktony.android.data.ArgumentsClean
import com.zktony.android.data.ArgumentsCurrent
import com.zktony.android.data.ArgumentsSpeed
import com.zktony.android.data.ArgumentsTemperature
import com.zktony.android.data.ArgumentsTransfer
import com.zktony.android.data.ArgumentsVoltage
import com.zktony.android.data.ExperimentalControl
import com.zktony.android.data.PipelineControl
import com.zktony.android.data.PumpControl
import com.zktony.android.data.VoltageControl
import com.zktony.android.data.toArguments
import com.zktony.android.data.toChannelState
import com.zktony.log.LogUtils
import com.zktony.serialport.command.Protocol
import com.zktony.serialport.ext.ascii2ByteArray
import com.zktony.serialport.ext.readInt8
import com.zktony.serialport.ext.toHexString
import com.zktony.serialport.lifecycle.SerialStoreUtils
import com.zktony.serialport.serialPortOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

object SerialPortUtils {

    fun with() {
        // 初始化rtu串口
        serialPortOf {
            device = "/dev/ttyS2"
        }?.let {
            SerialStoreUtils.put("A", it)
            LogUtils.info("下位机串口初始化完成: ID = A, Device = ttyS2", true)
        } ?: {
            LogUtils.error("下位机串口初始化化失败: ID = A, Device = ttyS2", true)
        }
        // 初始化tec串口
        serialPortOf {
            device = "/dev/ttyS0"
        }?.let {
            SerialStoreUtils.put("B", it)
            LogUtils.info("灯板串口初始化完成: ID = B, Device = ttyS0", true)
        } ?: {
            LogUtils.error("灯板串口初始化失败: ID = B, Device = ttyS0", true)
        }
    }

    // 写入
    suspend fun writeResult(
        target: Int,
        key: String,
        func: Byte,
        byteArray: ByteArray = byteArrayOf(),
        timeOut: Long = 1000
    ): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val bytesList = mutableListOf<ByteArray>()
        var rx = -1

        try {
            serialPort.registerCallback(key) { bytes ->
                bytesList.add(bytes)
                Protocol.verifyProtocol(bytes) {
                    if (it.function == func) {
                        rx = it.data.readInt8()
                    }
                }
            }
            // 设置参数
            val bytes = Protocol().apply {
                function = func
                targetAddress = (target + 2).toByte()
                data = byteArray
            }.serialization()
            // 发送命令
            serialPort.sendByteArray(bytes)
            bytesList.add(bytes)
            // 等待设置结果
            withTimeout(timeOut) {
                while (rx == -1) {
                    delay(10)
                }
            }
            if (rx == 1) throw Exception("Set Return Failed")
        } catch (e: Exception) {
            bytesList.forEach { LogUtils.error(key, it.toHexString(), true) }
            LogUtils.error(key, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(key)
        }

        return rx == 0
    }

    // 查询
    suspend fun queryResult(
        target: Int,
        key: String,
        func: Byte,
        byteArray: ByteArray = byteArrayOf(),
        timeOut: Long = 1000
    ): ByteArray? {
        val serialPort = SerialStoreUtils.get("A") ?: return null
        val bytesList = mutableListOf<ByteArray>()
        var ba: ByteArray? = null

        try {
            serialPort.registerCallback(key) { bytes ->
                bytesList.add(bytes)
                Protocol.verifyProtocol(bytes) {
                    if (it.function == func) {
                        ba = it.data
                    }
                }
            }
            // 设置参数
            val bytes = Protocol().apply {
                function = func
                targetAddress = (target + 2).toByte()
                data = byteArray
            }.serialization()
            // 发送命令
            serialPort.sendByteArray(bytes)
            bytesList.add(bytes)
            // 等待设置结果
            withTimeout(timeOut) {
                while (ba == null) {
                    delay(10)
                }
            }
            if (ba == null) throw Exception("Query Return Failed")
        } catch (e: Exception) {
            bytesList.forEach { LogUtils.error(key, it.toHexString(), true) }
            LogUtils.error(key, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(key)
        }

        return ba
    }

    // 设置电磁阀参数
    suspend fun setSolenoidValveArguments(target: Int, value: Int): Boolean {
        val byteArray = byteArrayOf(0x00, value.toByte())
        return writeResult(target, "SetSolenoidValveArguments", 0x05.toByte(), byteArray)
    }

    // 设置参数
    suspend fun setArguments(target: Int, args: Arguments): Boolean {
        try {
            val byteArray = args.toByteArray()
            return writeResult(target, "SetArguments", 0x12.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetArguments", e.stackTraceToString(), true)
            return false
        }
    }

    // 实验运行参数设置
    suspend fun setExperimentalArguments(target: Int, control: ExperimentalControl): Boolean {
        try {
            val byteArray = control.toByteArray()
            return writeResult(
                target,
                "SetExperimentalArguments",
                0x13.toByte(),
                byteArray
            )
        } catch (e: Exception) {
            LogUtils.error("SetExperimentalArguments", e.stackTraceToString(), true)
            return false
        }
    }

    // 实验运行状态设置
    suspend fun setExperimentalState(target: Int, state: Int): Boolean {
        try {
            val byteArray = byteArrayOf(state.toByte())
            return writeResult(
                target,
                "SetExperimentalState",
                0x14.toByte(),
                byteArray
            )
        } catch (e: Exception) {
            LogUtils.error("SetExperimentalState", e.stackTraceToString(), true)
            return false
        }
    }

    // 清洗管路
    suspend fun pipelineClean(target: Int, control: PipelineControl): Boolean {
        try {
            val byteArray = control.toByteArray()
            return writeResult(
                target,
                "PipelineClear",
                0x15.toByte(),
                byteArray
            )
        } catch (e: Exception) {
            LogUtils.error("PipelineClear", e.stackTraceToString(), true)
            return false
        }
    }

    // 填充管路
    suspend fun pipelineFill(target: Int, value: Int): Boolean {
        try {
            val byteArray = byteArrayOf(value.toByte())
            return writeResult(
                target,
                "PipelineFill",
                0x21.toByte(),
                byteArray
            )
        } catch (e: Exception) {
            LogUtils.error("PipelineFill", e.stackTraceToString(), true)
            return false
        }
    }

    // 管路排液
    suspend fun pipelineDrain(target: Int, value: Int): Boolean {
        try {
            val byteArray = byteArrayOf(value.toByte())
            return writeResult(
                target,
                "PipelineDrain",
                0x22.toByte(),
                byteArray
            )
        } catch (e: Exception) {
            LogUtils.error("PipelineDrain", e.stackTraceToString(), true)
            return false
        }
    }

    // 电机控制
    suspend fun startPump(target: Int, control: PumpControl): Boolean {
        try {
            val byteArray = control.toByteArray()
            return writeResult(
                target,
                "StartPump",
                0x23.toByte(),
                byteArray
            )
        } catch (e: Exception) {
            LogUtils.error("StartPump", e.stackTraceToString(), true)
            return false
        }
    }

    // 电机控制
    suspend fun stopPump(target: Int, value: Int): Boolean {
        try {
            val byteArray = byteArrayOf(value.toByte())
            return writeResult(
                target,
                "StopPump",
                0x24.toByte(),
                byteArray
            )
        } catch (e: Exception) {
            LogUtils.error("StopPump", e.stackTraceToString(), true)
            return false
        }
    }

    // 电极控制
    suspend fun startVoltage(target: Int, control: VoltageControl): Boolean {
        try {
            val byteArray = control.toByteArray()
            return writeResult(
                target,
                "StartVoltage",
                0x25.toByte(),
                byteArray
            )
        } catch (e: Exception) {
            LogUtils.error("StartVoltage", e.stackTraceToString(), true)
            return false
        }
    }

    // 电极控制
    suspend fun stopVoltage(target: Int): Boolean {
        try {
            return writeResult(
                target,
                "StopVoltage",
                0x26.toByte(),
                byteArrayOf()
            )
        } catch (e: Exception) {
            LogUtils.error("StopVoltage", e.stackTraceToString(), true)
            return false
        }
    }

    // 设置转膜参数
    suspend fun setTransferArguments(target: Int, args: ArgumentsTransfer): Boolean {
        try {
            val byteArray = args.toByteArray()
            return writeResult(target, "SetTransferArguments", 0x29.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetTransferArguments", e.stackTraceToString(), true)
            return false
        }
    }

    // 设置清洗参数
    suspend fun setCleanArguments(target: Int, args: ArgumentsClean): Boolean {
        try {
            val byteArray = args.toByteArray()
            return writeResult(target, "SetCleanArguments", 0x2A.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetCleanArguments", e.stackTraceToString(), true)
            return false
        }
    }

    // 设置温度参数
    suspend fun setTemperatureArguments(target: Int, args: ArgumentsTemperature): Boolean {
        try {
            val byteArray = args.toByteArray()
            return writeResult(target, "SetTemperatureArguments", 0x2B.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetTemperatureArguments", e.stackTraceToString(), true)
            return false
        }
    }

    // 设置电机参数
    suspend fun setSpeedArguments(target: Int, args: ArgumentsSpeed): Boolean {
        try {
            val byteArray = args.toByteArray()
            return writeResult(target, "SetSpeedArguments", 0x2C.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetSpeedArguments", e.stackTraceToString(), true)
            return false
        }
    }

    // 设置电压参数
    suspend fun setVoltageArguments(target: Int, args: ArgumentsVoltage): Boolean {
        try {
            val byteArray = args.toByteArray()
            return writeResult(target, "SetVoltageArguments", 0x2D.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetVoltageArguments", e.stackTraceToString(), true)
            return false
        }
    }

    // 设置电流参数
    suspend fun setCurrentArguments(target: Int, args: ArgumentsCurrent): Boolean {
        try {
            val byteArray = args.toByteArray()
            return writeResult(target, "SetCurrentArguments", 0x2E.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetCurrentArguments", e.stackTraceToString(), true)
            return false
        }
    }

    // 设置传感器参数
    suspend fun setSensorArguments(target: Int, args: ArgumentsBubble): Boolean {
        try {
            val byteArray = args.toByteArray()
            return writeResult(target, "SetSensorArguments", 0x2F.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetSensorArguments", e.stackTraceToString(), true)
            return false
        }
    }

    // 设置仪器PN号
    suspend fun setProductNumber(target: Int, pn: String): Boolean {
        try {
            val byteArray = pn.ascii2ByteArray(true)
            return writeResult(target, "SetProductNumber", 0x30.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetProductNumber", e.stackTraceToString(), true)
            return false
        }
    }

    // 设置仪器SN号
    suspend fun setSerialNumber(target: Int, sn: String): Boolean {
        try {
            val byteArray = sn.ascii2ByteArray(true)
            return writeResult(target, "SetSerialNumber", 0x31.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetSerialNumber", e.stackTraceToString(), true)
            return false
        }
    }

    // 查询 ChannelState
    suspend fun queryChannelState(target: Int): Boolean {
        try {
            val ba = queryResult(target, "QueryChannelState", 0x40.toByte(), byteArrayOf(), 200)
            if (ba != null) {
                toChannelState(ba)?.let { channelState ->
                    AppStateUtils.setChannelStateList(AppStateUtils.channelStateList.value.mapIndexed { index, state ->
                        if (index == target) {
                            channelState
                        } else {
                            state
                        }
                    })
                } ?: return false
                return true
            } else {
                return false
            }
        } catch (e: Exception) {
            LogUtils.error("QueryChannelState", e.stackTraceToString(), true)
            return false
        }
    }

    // 查询Arguments
    suspend fun queryArguments(target: Int): Boolean {
        try {
            val ba = queryResult(target, "QueryArguments", 0x41.toByte(), byteArrayOf(), 1000)
            if (ba != null) {
                toArguments(ba)?.let { arguments ->
                    AppStateUtils.setArgumentsList(AppStateUtils.argumentsList.value.mapIndexed { index, arg ->
                        if (index == target) {
                            arguments
                        } else {
                            arg
                        }
                    })
                } ?: return false
                return true
            } else {
                return false
            }
        } catch (e: Exception) {
            LogUtils.error("QueryArguments", e.stackTraceToString(), true)
            return false
        }
    }
}