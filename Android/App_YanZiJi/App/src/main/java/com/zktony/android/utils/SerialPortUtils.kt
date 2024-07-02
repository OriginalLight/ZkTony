package com.zktony.android.utils

import com.zktony.android.data.Arguments
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
            LogUtils.info("串口-B-灯板-ttyS0-初始化完成", true)
        } ?: {
            LogUtils.error("串口-B-灯板-ttyS0-初始化失败", true)
        }
    }

    // 设置仪器SN号
    suspend fun setSerialNumber(sn: String, target: Int) : Boolean {
        val serialPort = SerialStoreUtils.get("A") ?: return false
        val callbackKey = "SetSerialNumber"
        var success = false

        try {
            serialPort.registerCallback(callbackKey) { bytes ->
                LogUtils.info(callbackKey, "$target 接收到数据: ${bytes.toHexString()}", true)
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
            LogUtils.error(e.stackTraceToString(), true)
        }finally {
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
                LogUtils.info(callbackKey, "$target 接收到数据: ${bytes.toHexString()}", true)
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
            LogUtils.error(e.stackTraceToString(), true)
        }finally {
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
                LogUtils.info(callbackKey, "$target 接收到数据: ${bytes.toHexString()}", true)
                Protocol.verifyProtocol(bytes) {
                    if (it.function == 0x32.toByte()) {
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
                            LogUtils.error(callbackKey, "$target Arguments解析失败", true)
                        }
                    }
                }
            }
            // 查询Arguments
            val protocol = Protocol().apply {
                function = 0x32.toByte()
                targetAddress = target.toByte()
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
            LogUtils.error(e.stackTraceToString(), true)
        }finally {
            serialPort.unregisterCallback(callbackKey)
        }

        return success
    }
}