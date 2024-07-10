package com.zktony.android.utils

import com.zktony.android.data.ArgumentsBubble
import com.zktony.android.data.ArgumentsClean
import com.zktony.android.data.ArgumentsCurrent
import com.zktony.android.data.ArgumentsSpeed
import com.zktony.android.data.ArgumentsTemperature
import com.zktony.android.data.ArgumentsTransfer
import com.zktony.android.data.ArgumentsVoltage
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
            withTimeout(1000) {
                while (rx == -1) {
                    delay(100)
                }
            }
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
            withTimeout(1000) {
                while (rx == -1) {
                    delay(100)
                }
            }
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
            withTimeout(1000) {
                while (rx == -1) {
                    delay(100)
                }
            }
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
            withTimeout(1000) {
                while (rx == -1) {
                    delay(100)
                }
            }
        } catch (e: Exception) {
            bytesList.forEach { LogUtils.error(callbackKey, it.toHexString(), true) }
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return rx == 0
    }

    // 电极控制
    suspend fun startVoltage(target: Int, control: VoltageControl): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "StartVoltage"
        val bytesList = mutableListOf<ByteArray>()
        var rx = -1

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                bytesList.add(bytes)
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x25.toByte()) {
                        rx = it.data.readInt8()
                    }
                }
            }
            // 电极控制
            val bytes = Protocol().apply {
                function = 0x25.toByte()
                targetAddress = (target + 2).toByte()
                data = control.toByteArray()
            }.serialization()
            // 发送电极控制命令
            serialPort.sendByteArray(bytes)
            bytesList.add(bytes)
            // 等待设置结果
            withTimeout(1000) {
                while (rx == -1) {
                    delay(100)
                }
            }
            if (rx == 1) throw Exception("Set return failed")
        } catch (e: Exception) {
            bytesList.forEach { LogUtils.error(callbackKey, it.toHexString(), true) }
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return rx == 2
    }

    // 电极控制
    suspend fun stopVoltage(target: Int): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "StopVoltage"
        val bytesList = mutableListOf<ByteArray>()
        var rx = -1

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                bytesList.add(bytes)
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x26.toByte()) {
                        rx = it.data.readInt8()
                    }
                }
            }
            // 电极控制
            val bytes = Protocol().apply {
                function = 0x26.toByte()
                targetAddress = (target + 2).toByte()
            }.serialization()
            // 发送电极控制命令
            serialPort.sendByteArray(bytes)
            bytesList.add(bytes)
            // 等待设置结果
            withTimeout(1000) {
                while (rx == -1) {
                    delay(100)
                }
            }
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
            withTimeout(200) {
                while (rx == -1) {
                    delay(20)
                }
            }
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
            withTimeout(1000) {
                while (rx == -1) {
                    delay(100)
                }
            }
            if (rx == 1) throw Exception("Query return failed")
        } catch (e: Exception) {
            bytesList.forEach { LogUtils.error(callbackKey, it.toHexString(), true) }
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return rx == 0
    }

    // 设置温度参数
    suspend fun setArguments(target: Int, key: String, func: Byte, byteArray: ByteArray): Boolean {
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
            // 设置温度参数
            val bytes = Protocol().apply {
                function = func
                targetAddress = (target + 2).toByte()
                data = byteArray
            }.serialization()
            // 发送设置温度参数命令
            serialPort.sendByteArray(bytes)
            bytesList.add(bytes)
            // 等待设置结果
            withTimeout(1000) {
                while (rx == -1) {
                    delay(100)
                }
            }
            if (rx == 1) throw Exception("Set return failed")
        } catch (e: Exception) {
            bytesList.forEach { LogUtils.error(key, it.toHexString(), true) }
            LogUtils.error(key, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(key)
        }

        return rx == 0
    }

    // 设置转膜参数
    suspend fun setTransferArguments(transfer: ArgumentsTransfer, target: Int): Boolean {
        return setArguments(target, "SetTransferArguments", 0x29.toByte(), transfer.toByteArray())
    }

    // 设置清洗参数
    suspend fun setCleanArguments(clean: ArgumentsClean, target: Int): Boolean {
        return setArguments(target, "SetCleanArguments", 0x2A.toByte(), clean.toByteArray())
    }

    // 设置电机参数
    suspend fun setSpeedArguments(target: Int, args: ArgumentsSpeed): Boolean {
        return setArguments(target, "SetSpeedArguments", 0x2B.toByte(), args.toByteArray())
    }

    // 设置电压参数
    suspend fun setVoltageArguments(target: Int, args: ArgumentsVoltage): Boolean {
        return setArguments(target, "SetVoltageArguments", 0x2C.toByte(), args.toByteArray())
    }

    // 设置电流参数
    suspend fun setCurrentArguments(target: Int, args: ArgumentsCurrent): Boolean {
        return setArguments(target, "SetCurrentArguments", 0x2D.toByte(), args.toByteArray())
    }

    // 设置温度参数
    suspend fun setTemperatureArguments(target: Int, args: ArgumentsTemperature): Boolean {
        return setArguments(target, "SetTemperatureArguments", 0x2E.toByte(), args.toByteArray())
    }

    // 设置传感器参数
    suspend fun setSensorArguments(target: Int, args: ArgumentsBubble): Boolean {
        return setArguments(target, "SetSensorArguments", 0x2F.toByte(), args.toByteArray())
    }
}