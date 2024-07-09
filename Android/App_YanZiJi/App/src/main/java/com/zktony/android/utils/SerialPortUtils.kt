package com.zktony.android.utils

import com.zktony.android.data.ArgumentsClean
import com.zktony.android.data.ArgumentsSpeed
import com.zktony.android.data.ArgumentsTransfer
import com.zktony.android.data.PumpControl
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

    // 设置仪器SN号
    suspend fun setSerialNumber(sn: String, target: Int): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "SetSerialNumber"
        val bytesList = mutableListOf<ByteArray>()
        var rx = -1

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                bytesList.add(bytes)
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x31.toByte()) {
                        rx = it.data.readInt8()
                    }
                }
            }
            // 设置SN号
            val bytes = Protocol().apply {
                targetAddress = (target + 2).toByte()
                function = 0x31.toByte()
                data = sn.ascii2ByteArray(true)
            }.serialization()
            // 发送设置SN号命令
            serialPort.sendByteArray(bytes)
            bytesList.add(bytes)
            // 等待设置结果
            withTimeout(1000) { while (rx == -1) { delay(100) } }
            if (rx == 1) throw Exception("Set return failed")
        } catch (e: Exception) {
            bytesList.forEach { LogUtils.error(callbackKey, it.toHexString(), true) }
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return rx == 0
    }

    // 设置仪器PN号
    suspend fun setProductNumber(pn: String, target: Int): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "SetProductNumber"
        val bytesList = mutableListOf<ByteArray>()
        var rx = -1

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                bytesList.add(bytes)
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x30.toByte()) {
                        rx = it.data.readInt8()
                    }
                }
            }
            // 设置PN号
            val bytes = Protocol().apply {
                function = 0x30.toByte()
                targetAddress = (target + 2).toByte()
                data = byteArrayOf(ProductUtils.ProductNumberList.indexOf(pn).toByte())
            }.serialization()
            // 发送设置PN号命令
            serialPort.sendByteArray(bytes)
            bytesList.add(bytes)
            // 等待设置结果
            withTimeout(1000) { while (rx == -1) { delay(100) } }
            if (rx == 1) throw Exception("Set return failed")
        } catch (e: Exception) {
            bytesList.forEach { LogUtils.error(callbackKey, it.toHexString(), true) }
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return rx == 0
    }

    // 电机控制
    suspend fun startPump(target: Int, control: PumpControl): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "StartPump"
        val bytesList = mutableListOf<ByteArray>()
        var rx = -1

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                bytesList.add(bytes)
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x23.toByte()) {
                        rx = it.data.readInt8()
                    }
                }
            }
            // 电机控制
            val bytes = Protocol().apply {
                function = 0x23.toByte()
                targetAddress = (target + 2).toByte()
                data = control.toByteArray()
            }.serialization()
            // 发送电机控制命令
            serialPort.sendByteArray(bytes)
            bytesList.add(bytes)
            // 等待设置结果
            withTimeout(1000) { while (rx == -1) { delay(100) } }
            if (rx == 1) throw Exception("Set return failed")
        } catch (e: Exception) {
            bytesList.forEach { LogUtils.error(callbackKey, it.toHexString(), true) }
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return rx == 2
    }

    // 电机控制
    suspend fun stopPump(target: Int, index: Int): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "StopPump"
        val bytesList = mutableListOf<ByteArray>()
        var rx = -1

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                bytesList.add(bytes)
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x24.toByte()) {
                        rx = it.data.readInt8()
                    }
                }
            }
            // 电机控制
            val protocol = Protocol().apply {
                function = 0x24.toByte()
                targetAddress = (target + 2).toByte()
                data = byteArrayOf(index.toByte())
            }.serialization()
            // 发送电机控制命令
            serialPort.sendByteArray(protocol)
            bytesList.add(protocol)
            // 等待设置结果
            withTimeout(1000) { while (rx == -1) { delay(100) } }
        } catch (e: Exception) {
            bytesList.forEach { LogUtils.error(callbackKey, it.toHexString(), true) }
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return rx == 0
    }

    // 查询 ChannelState
    suspend fun queryChannelState(target: Int): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "QueryChannelState"
        val bytesList = mutableListOf<ByteArray>()
        var rx = -1

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                bytesList.add(bytes)
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x40.toByte()) {
                        toChannelState(it.data)?.let { channelState ->
                            AppStateUtils.setChannelStateList(AppStateUtils.channelStateList.value.mapIndexed { index, state ->
                                if (index == target) {
                                    channelState
                                } else {
                                    state
                                }
                            })
                            rx = 0
                        }
                    }
                }
            }
            // 查询ChannelState
            val bytes = Protocol().apply {
                function = 0x40.toByte()
                targetAddress = (target + 2).toByte()
            }.serialization()
            // 发送查询ChannelState命令
            serialPort.sendByteArray(bytes)
            bytesList.add(bytes)
            // 等待查询结果
            withTimeout(200) { while (rx == -1) { delay(20) } }
        } catch (e: Exception) {
            bytesList.forEach { LogUtils.error(callbackKey, it.toHexString(), true) }
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return rx == 0
    }

    // 查询Arguments
    suspend fun queryArguments(target: Int): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "QueryArguments"
        val bytesList = mutableListOf<ByteArray>()
        var rx = -1

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                bytesList.add(bytes)
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x41.toByte()) {
                        toArguments(it.data)?.let { arguments ->
                            AppStateUtils.setArgumentsList(AppStateUtils.argumentsList.value.mapIndexed { index, arg ->
                                if (index == target) {
                                    arguments
                                } else {
                                    arg
                                }
                            })
                            rx = 0
                        }
                    }
                }
            }
            // 查询Arguments
            val bytes = Protocol().apply {
                function = 0x41.toByte()
                targetAddress = (target + 2).toByte()
            }.serialization()
            // 发送查询Arguments命令
            serialPort.sendByteArray(bytes)
            bytesList.add(bytes)
            // 等待查询结果
            withTimeout(1000) { while (rx == -1) { delay(100) } }
            if (rx == 1) throw Exception("Query return failed")
        } catch (e: Exception) {
            bytesList.forEach { LogUtils.error(callbackKey, it.toHexString(), true) }
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return rx == 0
    }

    // 设置转膜参数
    suspend fun setTransferArguments(transfer: ArgumentsTransfer, target: Int): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "SetTransferArguments"
        val bytesList = mutableListOf<ByteArray>()
        var rx = -1

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                bytesList.add(bytes)
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x29.toByte()) {
                        rx = it.data.readInt8()
                    }
                }
            }
            // 设置转膜参数
            val bytes = Protocol().apply {
                function = 0x29.toByte()
                targetAddress = (target + 2).toByte()
                data = transfer.toByteArray()
            }.serialization()
            // 发送设置转膜参数命令
            serialPort.sendByteArray(bytes)
            bytesList.add(bytes)
            // 等待设置结果
            withTimeout(1000) { while (rx == -1) { delay(100) } }
            if (rx == 1) throw Exception("Set return failed")
        } catch (e: Exception) {
            bytesList.forEach { LogUtils.error(callbackKey, it.toHexString(), true) }
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return rx == 0
    }

    // 设置清洗参数
    suspend fun setCleanArguments(clean: ArgumentsClean, target: Int): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "SetCleanArguments"
        val bytesList = mutableListOf<ByteArray>()
        var rx = -1

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                bytesList.add(bytes)
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x2A.toByte()) {
                        rx = it.data.readInt8()
                    }
                }
            }
            // 设置清洗参数
            val bytes = Protocol().apply {
                function = 0x2A.toByte()
                targetAddress = (target + 2).toByte()
                data = clean.toByteArray()
            }.serialization()
            // 发送设置清洗参数命令
            serialPort.sendByteArray(bytes)
            bytesList.add(bytes)
            // 等待设置结果
            withTimeout(1000) { while (rx == -1) { delay(100) } }
            if (rx == 1) throw Exception("Set return failed")
        } catch (e: Exception) {
            bytesList.forEach { LogUtils.error(callbackKey, it.toHexString(), true) }
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return rx == 0
    }

    // 设置电机参数
    suspend fun setSpeedArguments(target: Int, args: ArgumentsSpeed): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "SetSpeedArguments"
        val bytesList = mutableListOf<ByteArray>()
        var rx = -1

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                bytesList.add(bytes)
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x2B.toByte()) {
                        rx = it.data.readInt8()
                    }
                }
            }
            // 设置泵参数
            val bytes = Protocol().apply {
                function = 0x2B.toByte()
                targetAddress = (target + 2).toByte()
                data = args.toByteArray()
            }.serialization()
            // 发送设置泵参数命令
            serialPort.sendByteArray(bytes)
            bytesList.add(bytes)
            // 等待设置结果
            withTimeout(1000) { while (rx == -1) { delay(100) } }
            if (rx == 1) throw Exception("Set return failed")
        } catch (e: Exception) {
            bytesList.forEach { LogUtils.error(callbackKey, it.toHexString(), true) }
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return rx == 0
    }
}