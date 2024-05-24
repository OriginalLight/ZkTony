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
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    return flow {
        try {
            var ready = false
            var finish = false
            // 注册回调
            SerialStoreUtils.get("rtu")?.registerCallback(key) { bytes ->
                Protocol.verifyProtocol(bytes) {
                    scope.launch {
                        // 升级命令是0xFD
                        if (it.func == 0xFD.toByte()) {
                            val index = it.data[0].toInt()
                            if (index == 0) {
                                ready = true
                            }
                            if (index == 1) {
                                finish = true
                                emit(UpgradeState.Success)
                            }
                            if (index == 2) {
                                finish = true
                                emit(UpgradeState.Err(Exception("升级失败")))
                            }
                        }
                    }
                }
            }
            val file = File(hexPath)
            if (!file.exists()) {
                throw Exception("文件不存在")
            }
            val byteArray = file.readBytes()
            val totalLength = byteArray.size
            val totalPackage = totalLength / byteLength + if (totalLength % byteLength == 0) 0 else 1
            // 发送升级准备命令
            SerialStoreUtils.get("rtu")?.sendByteArray(Protocol().apply {
                func = 0xFD.toByte()
                data = byteArrayOf(0x00, 0x00).writeInt16LE(totalLength)
            }.serialization())
            delay(1000)
            if (!ready) {
                throw Exception("升级命令响应失败")
            }
            // 发送升级数据
            for (i in 0 until totalPackage) {
                val start = i * byteLength
                val end = if (start + byteLength > totalLength) totalLength else start + byteLength
                val bytes = byteArray.copyOfRange(start, end)
                SerialStoreUtils.get("rtu")?.sendByteArray(Protocol().apply {
                    func = 0xFD.toByte()
                    data = byteArrayOf(0x00, 0x00).writeInt16LE(i + 1) + bytes
                }.serialization())
                emit(UpgradeState.Progress(i * 1.0 / totalPackage))
                delay(100)
            }
            withTimeout(10 * 1000L) {
                while (!finish) {
                    delay(100)
                }
            }
        } catch (e: Exception) {
            emit(UpgradeState.Err(e))
        } finally {
            SerialStoreUtils.get("rtu")?.unregisterCallback(key)
        }
    }
}

suspend fun embeddedVersion(): String {
    var version :String? = null
    val key = "embeddedVersion"
    try {
        // 注册回调
        SerialStoreUtils.get("rtu")?.registerCallback(key) { bytes ->
            Protocol.verifyProtocol(bytes) {
                if (it.func == 0xFE.toByte()) {
                    version = it.data.toAsciiString()
                }
            }
        }
        SerialStoreUtils.get("rtu")?.sendByteArray(Protocol().apply {
            func = 0xFE.toByte()
        }.serialization())
        withTimeout(1000L) {
            while (version == null) {
                delay(100)
            }
        }
    } catch (e: Exception) {
        Log.e("EmbeddedExt", e.message ?: "Unknown")
    } finally {
        // 取消注册
        SerialStoreUtils.get("rtu")?.unregisterCallback(key)
    }
    return version ?: "Unknown"
}

sealed class UpgradeState {
    data class Progress(val progress: Double) : UpgradeState()
    data class Err(val t: Throwable) : UpgradeState()
    data object Success : UpgradeState()
}

