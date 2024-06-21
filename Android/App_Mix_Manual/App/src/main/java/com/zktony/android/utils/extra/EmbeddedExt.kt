package com.zktony.android.utils.extra

import android.util.Log
import com.zktony.serialport.command.Protocol
import com.zktony.serialport.ext.readInt16LE
import com.zktony.serialport.ext.readInt8
import com.zktony.serialport.ext.toAsciiString
import com.zktony.serialport.ext.writeInt16LE
import com.zktony.serialport.ext.writeInt32LE
import com.zktony.serialport.lifecycle.SerialStoreUtils
import com.zktony.serialport.utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.File

suspend fun embeddedUpgrade(hexFile: File,keyName:String,serialName:String) = channelFlow {
    val key = keyName
    val byteLength = 1024
    val serialPort = SerialStoreUtils.get(serialName) ?: return@channelFlow
    try {
        var flag = -1
        var rx: Boolean
        // 注册回调
        serialPort.registerCallback(key) { bytes ->
            Protocol.verifyProtocol(bytes) {
                launch {
                    when(it.func) {
                        0xA0.toByte() -> {
                            log("EmbeddedExt", "升级准备就绪")
                            flag = if (it.data.readInt8() == 1) {
                                -1
                            } else {
                                0
                            }
                        }
                        0xA1.toByte() -> {
                            log("EmbeddedExt", "升级数据信息就绪")
                            flag = if (it.data.readInt8() == 1) {
                                -1
                            } else {
                                1
                            }
                        }
                        0xA2.toByte() -> {
                            log("EmbeddedExt", "地址擦除就绪")
                            flag = if (it.data.readInt8() == 1) {
                                -1
                            } else {
                                2
                            }
                        }
                        0xA3.toByte() -> {
                            if (it.data.readInt8() == 1) {
                                flag = -1
                            }
                            rx = true
                        }
                        0xA4.toByte() -> {
                            if (it.data.readInt8() == 1) {
                                flag = -1
                                send(UpgradeState.Err(Exception("升级失败")))
                                Log.e("EmbeddedExt", "升级失败")
                            } else {
                                send(UpgradeState.Success)
                                flag = 3
                                log("EmbeddedExt", "升级成功")
                            }
                        }
                        else -> {
                            log("EmbeddedExt", "未知命令")
                        }
                    }
                }
            }
        }

        // 读取文件
        val byteArray = hexFile.readBytes()
        val totalLength = byteArray.size
        val totalPackage = totalLength / byteLength + if (totalLength % byteLength == 0) 0 else 1

        // 发送升级准备命令
        send(UpgradeState.Message("升级准备中"))
        serialPort.sendByteArray(Protocol().apply { func = 0xA0.toByte() }.serialization())
        delay(300)
        if (flag != 0) error("升级准备响应失败")

        // 发送升级数据信息
        send(UpgradeState.Message("升级数据信息中"))
        serialPort.sendByteArray(Protocol().apply {
            func = 0xA1.toByte()
            data = byteArrayOf(0x00, 0x00).writeInt16LE(totalPackage) + byteArrayOf(0x00, 0x00).writeInt16LE(totalLength)
        }.serialization())
        delay(300)
        if (flag != 1) error("升级数据信息响应失败")

        // 地址擦除
        send(UpgradeState.Message("地址擦除中"))
        val startAddress = 0x8020000
        val endAddress = 0x8020000 + totalLength
        serialPort.sendByteArray(Protocol().apply {
            func = 0xA2.toByte()
            data = byteArrayOf(0x00, 0x00, 0x00, 0x00).writeInt32LE(startAddress.toLong()) + byteArrayOf(0x00, 0x00, 0x00, 0x00).writeInt32LE(endAddress.toLong())
        }.serialization())
        delay(2000)
        if (flag != 2) error("地址擦除响应失败")

        // 发送升级数据
        for (i in 0 until totalPackage) {
            // 擦除失败或者超时
            rx = false
            val start = i * byteLength
            val end = if (start + byteLength > totalLength) totalLength else start + byteLength
            val bytes = byteArray.copyOfRange(start, end)
            // 发送升级数据
            serialPort.sendByteArray(Protocol().apply {
                func = 0xA3.toByte()
                data = byteArrayOf(0x00, 0x00).writeInt16LE(i) + bytes
            }.serialization())
            // 等待升级数据响应
            try {
                withTimeout(1000) {
                    while (!rx) {
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
        serialPort.sendByteArray(Protocol().apply { func = 0xA4.toByte() }.serialization())
        // 等待升级完成响应
        withTimeout(2000L) {
            while (flag != 3) {
                delay(30)
            }
        }
    } catch (e: Exception) {
        Log.e("EmbeddedExt", e.message ?: "Unknown Error")
        send(UpgradeState.Err(e))
    } finally {
        serialPort.unregisterCallback(key)
    }
}

suspend fun embeddedVersion(): String {
    var version = "Unknown"
    val key = "embeddedVersion"
    val serialPort = SerialStoreUtils.get("zkty") ?: return version
    try {
        // 注册回调
        serialPort.registerCallback(key) { bytes ->
            Protocol.verifyProtocol(bytes) {
                if (it.func == 0x0A.toByte()) {
                    version = it.data.toAsciiString()
                }
            }
        }
        // 发送版本查询命令
        serialPort.sendByteArray(Protocol().apply {
            func = 0x0A.toByte()
        }.serialization())
        // 等待版本响应
        withTimeout(2000L) {
            while (version == "Unknown") {
                delay(30)
            }
        }
    } catch (e: Exception) {
        Log.e("EmbeddedExt", e.message ?: "Unknown")
    } finally {
        // 取消注册
        serialPort.unregisterCallback(key)
    }
    // 返回版本号
    return version
}

sealed class UpgradeState {
    data class Message(val message: String) : UpgradeState()
    data class Progress(val progress: Double) : UpgradeState()
    data class Err(val t: Throwable) : UpgradeState()
    data object Success : UpgradeState()
}