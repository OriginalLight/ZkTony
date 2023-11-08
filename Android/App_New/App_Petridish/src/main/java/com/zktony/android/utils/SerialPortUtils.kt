package com.zktony.android.utils

import com.zktony.android.utils.AppStateUtils.hpa
import com.zktony.android.utils.AppStateUtils.hpg
import com.zktony.serialport.abstractSerialHelperOf
import com.zktony.serialport.command.Protocol
import com.zktony.serialport.ext.readInt8
import com.zktony.serialport.lifecycle.SerialStoreUtils

object SerialPortUtils {
    fun with() {
        // 初始化zkty串口
        SerialStoreUtils.put("zkty", abstractSerialHelperOf {
            device = "/dev/ttyS0"
            log = true
        })

        // rtu串口全局回调
        SerialStoreUtils.get("zkty")?.callbackHandler = { bytes ->
            Protocol.Protocol.callbackHandler(bytes) { code, rx ->
                when (code) {
                    Protocol.RX_0X01 -> {
                        for (i in 0 until rx.data.size / 2) {
                            val index = rx.data.readInt8(offset = i * 2)
                            val status = rx.data.readInt8(offset = i * 2 + 1)
                            hpa[index] = status == 1
                        }
                    }

                    Protocol.RX_0X02 -> {
                        for (i in 0 until rx.data.size / 2) {
                            val index = rx.data.readInt8(offset = i * 2)
                            val status = rx.data.readInt8(offset = i * 2 + 1)
                            hpg[index] = status == 1
                        }
                    }
                }
            }
        }
    }
}