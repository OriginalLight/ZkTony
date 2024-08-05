package com.zktony.android.utils

import com.zktony.android.data.Arguments
import com.zktony.android.data.ArgumentsBubble
import com.zktony.android.data.ArgumentsClean
import com.zktony.android.data.ArgumentsCurrent
import com.zktony.android.data.ArgumentsSpeed
import com.zktony.android.data.ArgumentsTemperature
import com.zktony.android.data.ArgumentsTransfer
import com.zktony.android.data.ArgumentsVoltage
import com.zktony.android.data.ChannelState
import com.zktony.android.data.ExperimentalControl
import com.zktony.android.data.PipelineControl
import com.zktony.android.data.PumpControl
import com.zktony.android.data.UpgradeState
import com.zktony.android.data.VoltageControl
import com.zktony.android.data.toArguments
import com.zktony.log.LogUtils
import com.zktony.serialport.ext.ascii2ByteArray
import com.zktony.serialport.ext.readInt8
import com.zktony.serialport.ext.toAsciiString
import com.zktony.serialport.ext.toHexString
import com.zktony.serialport.ext.writeInt16LE
import com.zktony.serialport.ext.writeInt32LE
import com.zktony.serialport.lifecycle.SerialStoreUtils
import com.zktony.serialport.protocol.ZktyProtocol
import com.zktony.serialport.protocol.zktyProtocolOf
import com.zktony.serialport.serialPortOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.io.File

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
    private suspend fun write(
        target: Int,
        key: String,
        func: Byte,
        byteArray: ByteArray,
        timeOut: Long,
        device: String = "A"
    ): Boolean {
        val serialPort = SerialStoreUtils.get(device) ?: return false
        val bytesList = mutableListOf<ByteArray>()
        var rx = -1

        try {
            serialPort.registerCallback(key) { bytes ->
                bytesList.add(bytes)
                ZktyProtocol.verifyProtocol(bytes) {
                    if (it.func == func) {
                        rx = it.data.readInt8()
                    }
                }
            }
            // 设置参数
            val bytes = zktyProtocolOf {
                this.func = func
                this.target = (target + 2).toByte()
                data = byteArray
            }
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
    private suspend fun query(
        target: Int,
        key: String,
        func: Byte,
        byteArray: ByteArray,
        timeOut: Long,
        device: String = "A"
    ): ByteArray? {
        val serialPort = SerialStoreUtils.get(device) ?: return null
        val bytesList = mutableListOf<ByteArray>()
        var ba: ByteArray? = null

        try {
            serialPort.registerCallback(key) { bytes ->
                bytesList.add(bytes)
                ZktyProtocol.verifyProtocol(bytes) {
                    if (it.func == func) {
                        ba = it.data
                    }
                }
            }
            // 设置参数
            val bytes = zktyProtocolOf {
                this.func = func
                this.target = (target + 2).toByte()
                data = byteArray
            }
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

    // 写入同时暂停轮询
    private suspend fun writeAndPausePolling(
        target: Int,
        key: String,
        func: Byte,
        byteArray: ByteArray = byteArrayOf(),
        timeOut: Long = 1000
    ): Boolean {
        AppStateUtils.isPolling.withLock {
            delay(100L)
            val result = write(target, key, func, byteArray, timeOut)
            return result
        }
    }

    // 查询同时暂停轮询
    private suspend fun queryAndPausePolling(
        target: Int,
        key: String,
        func: Byte,
        byteArray: ByteArray = byteArrayOf(),
        timeOut: Long = 1000
    ): ByteArray? {
        AppStateUtils.isPolling.withLock {
            delay(100L)
            val result = query(target, key, func, byteArray, timeOut)
            return result
        }
    }

    // 设置电磁阀参数
    suspend fun setSolenoidValveArguments(target: Int, value: Int): Boolean {
        val byteArray = byteArrayOf(0x00, value.toByte())
        return writeAndPausePolling(target, "SetSolenoidValveArguments", 0x05.toByte(), byteArray)
    }

    // 设置参数
    suspend fun setArguments(target: Int, args: Arguments): Boolean {
        try {
            val byteArray = args.toByteArray()
            return writeAndPausePolling(target, "SetArguments", 0x12.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetArguments", e.stackTraceToString(), true)
            return false
        }
    }

    // 实验运行参数设置
    suspend fun setExperimentalArguments(target: Int, control: ExperimentalControl): Boolean {
        try {
            val byteArray = control.toByteArray()
            return writeAndPausePolling(
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
            return writeAndPausePolling(
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
            return writeAndPausePolling(
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
            return writeAndPausePolling(
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
            return writeAndPausePolling(
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
            return writeAndPausePolling(
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
            return writeAndPausePolling(
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
            return writeAndPausePolling(
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
            return writeAndPausePolling(
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
            return writeAndPausePolling(target, "SetTransferArguments", 0x29.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetTransferArguments", e.stackTraceToString(), true)
            return false
        }
    }

    // 设置清洗参数
    suspend fun setCleanArguments(target: Int, args: ArgumentsClean): Boolean {
        try {
            val byteArray = args.toByteArray()
            return writeAndPausePolling(target, "SetCleanArguments", 0x2A.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetCleanArguments", e.stackTraceToString(), true)
            return false
        }
    }

    // 设置温度参数
    suspend fun setTemperatureArguments(target: Int, args: ArgumentsTemperature): Boolean {
        try {
            val byteArray = args.toByteArray()
            return writeAndPausePolling(target, "SetTemperatureArguments", 0x2B.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetTemperatureArguments", e.stackTraceToString(), true)
            return false
        }
    }

    // 设置电机参数
    suspend fun setSpeedArguments(target: Int, args: ArgumentsSpeed): Boolean {
        try {
            val byteArray = args.toByteArray()
            return writeAndPausePolling(target, "SetSpeedArguments", 0x2C.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetSpeedArguments", e.stackTraceToString(), true)
            return false
        }
    }

    // 设置电压参数
    suspend fun setVoltageArguments(target: Int, args: ArgumentsVoltage): Boolean {
        try {
            val byteArray = args.toByteArray()
            return writeAndPausePolling(target, "SetVoltageArguments", 0x2D.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetVoltageArguments", e.stackTraceToString(), true)
            return false
        }
    }

    // 设置电流参数
    suspend fun setCurrentArguments(target: Int, args: ArgumentsCurrent): Boolean {
        try {
            val byteArray = args.toByteArray()
            return writeAndPausePolling(target, "SetCurrentArguments", 0x2E.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetCurrentArguments", e.stackTraceToString(), true)
            return false
        }
    }

    // 设置传感器参数
    suspend fun setSensorArguments(target: Int, args: ArgumentsBubble): Boolean {
        try {
            val byteArray = args.toByteArray()
            return writeAndPausePolling(target, "SetSensorArguments", 0x2F.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetSensorArguments", e.stackTraceToString(), true)
            return false
        }
    }

    // 设置仪器PN号
    suspend fun setProductNumber(target: Int, pn: String): Boolean {
        try {
            val byteArray = pn.ascii2ByteArray(true)
            return writeAndPausePolling(target, "SetProductNumber", 0x30.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetProductNumber", e.stackTraceToString(), true)
            return false
        }
    }

    // 设置仪器SN号
    suspend fun setSerialNumber(target: Int, sn: String): Boolean {
        try {
            val byteArray = sn.ascii2ByteArray(true)
            return writeAndPausePolling(target, "SetSerialNumber", 0x31.toByte(), byteArray)
        } catch (e: Exception) {
            LogUtils.error("SetSerialNumber", e.stackTraceToString(), true)
            return false
        }
    }

    // 查询 ChannelState
    suspend fun queryChannelState(target: Int): Boolean {
        try {
            val ba = query(target, "QueryChannelState", 0x40.toByte(), byteArrayOf(), 100)
            if (ba != null) {
                ChannelState.fromByteArray(ba)?.let { channelState ->
                    AppStateUtils.setExperimentalStateHook(target, channelState)
                    AppStateUtils.setChannelState(target, channelState)
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
            val ba =
                queryAndPausePolling(target, "QueryArguments", 0x41.toByte(), byteArrayOf(), 1000)
            if (ba != null) {
                toArguments(ba)?.let { arguments ->
                    AppStateUtils.setArguments(target, arguments)
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

    // 查询版本
    suspend fun queryVersion(target: Int, device: String = "A"): String {
        try {
            val ba = query(
                target,
                "QueryVersion",
                0x50.toByte(),
                byteArrayOf(0x00.toByte(), 0x00.toByte()),
                200,
                device
            )
            LogUtils.info("QueryVersion", ba?.toHexString() ?: "Unknown", true)
            return ba?.toAsciiString() ?: "Unknown"
        } catch (e: Exception) {
            LogUtils.error("QueryVersion", e.stackTraceToString(), true)
            return "Unknown"
        }
    }

    // 升级
    suspend fun upgrade(hexFile: File, device: String, target: Int) = channelFlow {
        AppStateUtils.isPolling.withLock {
            delay(100L)
            val key = "upgrade"
            val byteLength = 1024
            val serialPort = SerialStoreUtils.get(device) ?: return@channelFlow
            val bytesList = mutableListOf<ByteArray>()
            var flag = -1

            try {
                // 注册回调
                serialPort.registerCallback(key) { bytes ->
                    ZktyProtocol.verifyProtocol(bytes) {
                        launch {
                            when (it.func) {
                                0xA0.toByte() -> {
                                    LogUtils.info(key, "升级准备就绪")
                                    flag = if (it.data.readInt8() == 1) {
                                        -1
                                    } else {
                                        0
                                    }
                                }

                                0xA1.toByte() -> {
                                    LogUtils.info(key, "升级数据信息就绪")
                                    flag = if (it.data.readInt8() == 1) {
                                        -1
                                    } else {
                                        1
                                    }
                                }

                                0xA2.toByte() -> {
                                    LogUtils.info(key, "地址擦除就绪")
                                    flag = if (it.data.readInt8() == 1) {
                                        -1
                                    } else {
                                        2
                                    }
                                }

                                0xA3.toByte() -> {
                                    flag = if (it.data.readInt8() == 1) {
                                        -1
                                    } else {
                                        3
                                    }
                                }

                                0xA4.toByte() -> {
                                    if (it.data.readInt8() == 1) {
                                        flag = -1
                                        send(UpgradeState.Err(Exception("升级失败")))
                                        LogUtils.error(key, "升级失败", true)
                                    } else {
                                        send(UpgradeState.Success)
                                        flag = 4
                                        LogUtils.info(key, "升级成功", true)
                                    }
                                }

                                else -> {
                                    LogUtils.error(key, "未知命令", true)
                                }
                            }
                        }
                    }
                }

                // 读取文件
                val byteArray = hexFile.readBytes()
                val totalLength = byteArray.size
                val totalPackage =
                    totalLength / byteLength + if (totalLength % byteLength == 0) 0 else 1

                // 发送升级准备命令
                send(UpgradeState.Message("升级准备中"))
                val b1 = zktyProtocolOf {
                    this.target = (target + 2).toByte()
                    func = 0xA0.toByte()
                }
                serialPort.sendByteArray(b1)
                bytesList.add(b1)
                delay(300)
                if (flag != 0) error("升级准备响应失败")

                // 发送升级数据信息
                send(UpgradeState.Message("升级数据信息中"))
                val b2 = zktyProtocolOf {
                    this.target = (target + 2).toByte()
                    func = 0xA1.toByte()
                    data = byteArrayOf(0x00, 0x00).writeInt16LE(totalPackage) + byteArrayOf(
                        0x00,
                        0x00
                    ).writeInt16LE(totalLength)
                }
                serialPort.sendByteArray(b2)
                bytesList.add(b2)
                delay(300)
                if (flag != 1) error("升级数据信息响应失败")

                // 地址擦除
                send(UpgradeState.Message("地址擦除中"))
                val startAddress = 0x8020000
                val endAddress = 0x8020000 + totalLength
                val b3 = zktyProtocolOf {
                    this.target = (target + 2).toByte()
                    func = 0xA2.toByte()
                    data = byteArrayOf(0x00, 0x00, 0x00, 0x00).writeInt32LE(startAddress.toLong()) +
                            byteArrayOf(0x00, 0x00, 0x00, 0x00).writeInt32LE(endAddress.toLong())
                }
                serialPort.sendByteArray(b3)
                bytesList.add(b3)
                delay(2000)
                if (flag != 2) error("地址擦除响应失败")

                // 发送升级数据
                for (i in 0 until totalPackage) {
                    // 擦除失败或者超时
                    flag = -1
                    val start = i * byteLength
                    val end =
                        if (start + byteLength > totalLength) totalLength else start + byteLength
                    val bytes = byteArray.copyOfRange(start, end)
                    // 发送升级数据
                    val b4 = zktyProtocolOf {
                        this.target = (target + 2).toByte()
                        func = 0xA3.toByte()
                        data = byteArrayOf(0x00, 0x00).writeInt16LE(i) + bytes
                    }
                    serialPort.sendByteArray(b4)
                    bytesList.add(b4)
                    // 等待升级数据响应
                    try {
                        withTimeout(1000) {
                            while (flag != 3) {
                                delay(30)
                            }
                        }
                        send(UpgradeState.Progress((i + 1) * 1.0 / totalPackage))
                    } catch (e: Exception) {
                        error("升级数据响应超时")
                    }
                }

                // 发送升级完成命令
                send(UpgradeState.Message("等待升级完成"))
                val b5 = zktyProtocolOf {
                    this.target = (target + 2).toByte()
                    func = 0xA4.toByte()
                }
                serialPort.sendByteArray(b5)
                bytesList.add(b5)
                // 等待升级完成响应
                try {
                    withTimeout(2000L) {
                        while (flag != 4) {
                            delay(30)
                        }
                    }
                } catch (e: Exception) {
                    error("升级完成响应超时")
                }
            } catch (e: Exception) {
                send(UpgradeState.Err(e))
                bytesList.forEach { LogUtils.error(key, it.toHexString(), true) }
                LogUtils.error(key, e.stackTraceToString(), true)
            } finally {
                serialPort.unregisterCallback(key)
            }
        }
    }
}