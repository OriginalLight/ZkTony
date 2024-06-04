package com.zktony.android.utils.extra

import android.util.Log
import com.zktony.serialport.command.Protocol
import com.zktony.serialport.ext.toAsciiString
import com.zktony.serialport.ext.writeInt16LE
import com.zktony.serialport.lifecycle.SerialStoreUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.File

suspend fun embeddedUpgrade(hexPath: String): Flow<UpgradeState> {
    val key = "embeddedUpgrade"
    val byteLength = 1024
    val serialPort = SerialStoreUtils.get("rtu") ?: return flow { emit(UpgradeState.Err(Exception("串口未初始化"))) }
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    return flow {
        try {
            var ready = false
            var finish = false
            // 注册回调
            serialPort.registerCallback(key) { bytes ->
                Protocol.verifyProtocol(bytes) {
                    scope.launch {
                        // 升级命令是0xFD
                        if (it.func == 0xFD.toByte()) {
                            when(it.data[1].toInt()) {
                                0 -> {
                                    Log.d("EmbeddedExt", "升级准备中")
                                    ready = true
                                }
                                1 -> {
                                    Log.d("EmbeddedExt", "升级成功")
                                    finish = true
                                    emit(UpgradeState.Success)
                                }
                                2 -> {
                                    Log.d("EmbeddedExt", "升级失败")
                                    finish = true
                                    emit(UpgradeState.Err(Exception("升级失败")))
                                }
                                else -> Log.d("EmbeddedExt", "未知状态")
                            }
                        }
                    }
                }
            }
            // 读取文件
            val file = File(hexPath)
            if (!file.exists()) error("文件不存在")
            val byteArray = file.readBytes()
            val totalLength = byteArray.size
            val totalPackage = totalLength / byteLength + if (totalLength % byteLength == 0) 0 else 1
            // 发送升级准备命令
            serialPort.sendByteArray(Protocol().apply {
                func = 0xFD.toByte()
                data = byteArrayOf(0x00, 0x00).writeInt16LE(totalLength)
            }.serialization())
            // 等待升级准备响应
            delay(1000)
            if (!ready) error("升级命令响应失败")
            // 发送升级数据
            for (i in 0 until totalPackage) {
                val start = i * byteLength
                val end = if (start + byteLength > totalLength) totalLength else start + byteLength
                val bytes = byteArray.copyOfRange(start, end)
                // 发送升级数据
                serialPort.sendByteArray(Protocol().apply {
                    func = 0xFD.toByte()
                    data = byteArrayOf(0x00, 0x00).writeInt16LE(i + 1) + bytes
                }.serialization())
                emit(UpgradeState.Progress(i * 1.0 / totalPackage))
                delay(100)
            }
            // 等待升级完成
            withTimeout(10 * 1000L) {
                while (!finish) {
                    delay(100)
                }
            }
        } catch (e: Exception) {
            Log.e("EmbeddedExt", e.message ?: "Unknown")
            emit(UpgradeState.Err(e))
        } finally {
            serialPort.unregisterCallback(key)
        }
    }
}

suspend fun embeddedVersion(): String {
    var version = "Unknown"
    val key = "embeddedVersion"
    val serialPort = SerialStoreUtils.get("rtu") ?: return version
    try {
        // 注册回调
        serialPort.registerCallback(key) { bytes ->
            Protocol.verifyProtocol(bytes) {
                if (it.func == 0xFE.toByte()) {
                    version = it.data.toAsciiString()
                }
            }
        }
        // 发送版本查询命令
        serialPort.sendByteArray(Protocol().apply {
            func = 0xFE.toByte()
        }.serialization())
        // 等待版本响应
        withTimeout(1000L) {
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
    data class Progress(val progress: Double) : UpgradeState()
    data class Err(val t: Throwable) : UpgradeState()
    data object Success : UpgradeState()
}

