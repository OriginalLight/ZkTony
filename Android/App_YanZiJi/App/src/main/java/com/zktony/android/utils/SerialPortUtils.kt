package com.zktony.android.utils

import com.zktony.android.data.ArgumentsClean
import com.zktony.android.data.ArgumentsSpeed
import com.zktony.android.data.ArgumentsTransfer
import com.zktony.android.data.PumpControl
import com.zktony.android.data.toArguments
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

        SerialStoreUtils.get("A")?.registerCallback("GlobeLogger") { bytes ->
            LogUtils.info("A 收到数据: ${bytes.toHexString()}", true)
        }

        SerialStoreUtils.get("B")?.registerCallback("GlobeLogger") { bytes ->
            LogUtils.info("B 收到数据: ${bytes.toHexString()}", true)
        }
    }

    // 设置仪器SN号
    suspend fun setSerialNumber(sn: String, target: Int): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "SetSerialNumber"
        var success = false

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x31.toByte()) {
                        success = it.data.readInt8() == 0
                    }
                }
            }
            // 设置SN号
            val protocol = Protocol().apply {
                targetAddress = target.toByte()
                function = 0x31.toByte()
                data = sn.ascii2ByteArray(true)
            }.serialization()
            LogUtils.info(callbackKey, "$target 发送数据: ${protocol.toHexString()}", true)
            // 发送设置SN号命令
            serialPort.sendByteArray(protocol)
            // 等待设置结果
            withTimeout(1000) {
                while (!success) {
                    delay(100)
                }
            }
        } catch (e: Exception) {
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return success
    }

    // 设置仪器PN号
    suspend fun setProductNumber(pn: String, target: Int): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "SetProductNumber"
        var success = false

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x30.toByte()) {
                        success = it.data.readInt8() == 0
                    }
                }
            }
            // 设置PN号
            val protocol = Protocol().apply {
                function = 0x30.toByte()
                targetAddress = target.toByte()
                data = byteArrayOf(ProductUtils.ProductNumberList.indexOf(pn).toByte())
            }.serialization()
            LogUtils.info(callbackKey, "$target 发送数据: ${protocol.toHexString()}", true)
            // 发送设置PN号命令
            serialPort.sendByteArray(protocol)
            // 等待设置结果
            withTimeout(1000) {
                while (!success) {
                    delay(100)
                }
            }
        } catch (e: Exception) {
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)

        }

        return success
    }

    // 电机控制
    suspend fun startPump(target: Int, control: PumpControl): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "StartPump"
        var success = false

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x23.toByte()) {
                        success = it.data.readInt8() == 2
                    }
                }
            }
            // 电机控制
            val protocol = Protocol().apply {
                function = 0x23.toByte()
                targetAddress = target.toByte()
                data = control.toByteArray()
            }.serialization()
            LogUtils.info(callbackKey, "$target 发送数据: ${protocol.toHexString()}", true)
            // 发送电机控制命令
            serialPort.sendByteArray(protocol)
            // 等待设置结果
            withTimeout(1000) {
                while (!success) {
                    delay(100)
                }
            }
        } catch (e: Exception) {
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return success
    }

    // 电机控制
    suspend fun stopPump(target: Int, index: Int): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "StopPump"
        var success = false

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x24.toByte()) {
                        success = it.data.readInt8() == 0
                    }
                }
            }
            // 电机控制
            val protocol = Protocol().apply {
                function = 0x24.toByte()
                targetAddress = target.toByte()
                data = byteArrayOf(index.toByte())
            }.serialization()
            LogUtils.info(callbackKey, "$target 发送数据: ${protocol.toHexString()}", true)
            // 发送电机控制命令
            serialPort.sendByteArray(protocol)
            // 等待设置结果
            withTimeout(1000) {
                while (!success) {
                    delay(100)
                }
            }
        } catch (e: Exception) {
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return success
    }

    // 查询Arguments
    suspend fun queryArguments(target: Int): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "QueryArguments"
        var success = false

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x41.toByte()) {
                        val arguments = toArguments(it.data)
                        if (arguments != null) {
                            AppStateUtils.setArgumentsList(AppStateUtils.argumentsList.value.mapIndexed { index, arg ->
                                if (index == target) {
                                    arguments
                                } else {
                                    arg
                                }
                            })
                            success = true
                        } else {
                            LogUtils.error(callbackKey, "$target 基本参数解析失败", true)
                        }
                    }
                }
            }
            // 查询Arguments
            val protocol = Protocol().apply {
                function = 0x41.toByte()
                targetAddress = target.toByte()
                data = byteArrayOf(0x00, 0x00)
            }.serialization()
            LogUtils.info(callbackKey, "$target 发送数据: ${protocol.toHexString()}", true)
            // 发送查询Arguments命令
            serialPort.sendByteArray(protocol)
            // 等待查询结果
            withTimeout(1000) {
                while (!success) {
                    delay(100)
                }
            }
        } catch (e: Exception) {
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return success
    }

    // 设置转膜参数
    suspend fun setTransferArguments(transfer: ArgumentsTransfer, target: Int): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "SetTransferArguments"
        var success = false

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x29.toByte()) {
                        success = it.data.readInt8() == 0
                    }
                }
            }
            // 设置转膜参数
            val protocol = Protocol().apply {
                function = 0x29.toByte()
                targetAddress = target.toByte()
                data = transfer.toByteArray()
            }.serialization()
            LogUtils.info(callbackKey, "$target 发送数据: ${protocol.toHexString()}", true)
            // 发送设置转膜参数命令
            serialPort.sendByteArray(protocol)
            // 等待设置结果
            withTimeout(1000) {
                while (!success) {
                    delay(100)
                }
            }
        } catch (e: Exception) {
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return success
    }

    // 设置清洗参数
    suspend fun setCleanArguments(clean: ArgumentsClean, target: Int): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "SetCleanArguments"
        var success = false

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x2A.toByte()) {
                        success = it.data.readInt8() == 0
                    }
                }
            }
            // 设置清洗参数
            val protocol = Protocol().apply {
                function = 0x2A.toByte()
                targetAddress = target.toByte()
                data = clean.toByteArray()
            }.serialization()
            LogUtils.info(callbackKey, "$target 发送数据: ${protocol.toHexString()}", true)
            // 发送设置清洗参数命令
            serialPort.sendByteArray(protocol)
            // 等待设置结果
            withTimeout(1000) {
                while (!success) {
                    delay(100)
                }
            }
        } catch (e: Exception) {
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return success
    }

    // 设置电机参数
    suspend fun setSpeedArguments(target: Int, args: ArgumentsSpeed): Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "SetSpeedArguments"
        var success = false

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x2B.toByte()) {
                        success = it.data.readInt8() == 0
                    }
                }
            }
            // 设置泵参数
            val protocol = Protocol().apply {
                function = 0x2B.toByte()
                targetAddress = target.toByte()
                data = args.toByteArray()
            }.serialization()
            LogUtils.info(callbackKey, "$target 发送数据: ${protocol.toHexString()}", true)
            // 发送设置泵参数命令
            serialPort.sendByteArray(protocol)
            // 等待设置结果
            withTimeout(1000) {
                while (!success) {
                    delay(100)
                }
            }
        } catch (e: Exception) {
            LogUtils.error(callbackKey, e.stackTraceToString(), true)
        } finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return success
    }
}