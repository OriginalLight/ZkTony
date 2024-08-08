package com.zktony.android.utils

import com.zktony.android.utils.AppStateUtils.hpa
import com.zktony.android.utils.AppStateUtils.hpg
import com.zktony.serialport.command.Protocol
import com.zktony.serialport.ext.readInt16LE
import com.zktony.serialport.ext.readInt8
import com.zktony.serialport.lifecycle.SerialStoreUtils
import com.zktony.serialport.serialPortOf

object SerialPortUtils {
    fun with() {
        // 初始化zkty串口
        SerialStoreUtils.put("zkty", serialPortOf {
            log = true
            device = "/dev/ttyS0"
        })

        // rtu串口全局回调
        SerialStoreUtils.get("zkty")?.callbackHandler = { bytes ->
            Protocol.verifyProtocol(bytes) { protocol ->
                if (protocol.func == 0xFF.toByte()) {
                    when (protocol.data.readInt16LE()) {
                        1 -> throw Exception("TX Header Error")
                        2 -> throw Exception("TX Addr Error")
                        3 -> throw Exception("TX Crc Error")
                        4 -> throw Exception("TX No Com")
                    }
                } else {
                    when (protocol.func) {
                        0x01.toByte() -> {
                            for (i in 0 until protocol.data.size / 2) {
                                val index = protocol.data.readInt8(offset = i * 2)
                                val status = protocol.data.readInt8(offset = i * 2 + 1)
                                hpa[index] = status == 1
                            }
                        }

                        0x02.toByte() -> {
                            for (i in 0 until protocol.data.size / 2) {
                                val index = protocol.data.readInt8(offset = i * 2)
                                val status = protocol.data.readInt8(offset = i * 2 + 1)
                                hpg[index] = status == 1
                                println("回复的光电信息===index:$index")
                                println("回复的光电信息===status:$status")
                                println("回复的光电信息===hpg[index]:${hpg[index]}")
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}