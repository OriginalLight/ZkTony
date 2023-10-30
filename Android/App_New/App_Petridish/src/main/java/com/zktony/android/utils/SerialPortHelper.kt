package com.zktony.android.utils

import com.zktony.android.utils.ext.loge
import com.zktony.serialport.AbstractSerialHelper
import com.zktony.serialport.command.Protocol
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.readInt8
import java.util.concurrent.CopyOnWriteArrayList

class SerialPortHelper : AbstractSerialHelper(
    SerialConfig(
        device = "/dev/ttyS0"
    )
) {

    val axis: CopyOnWriteArrayList<Boolean> = CopyOnWriteArrayList<Boolean>().apply {
        repeat(16) { add(false) }
    }
    val gpio: CopyOnWriteArrayList<Boolean> = CopyOnWriteArrayList<Boolean>().apply {
        repeat(16) { add(false) }
    }

    override fun callbackHandler(byteArray: ByteArray) {
        Protocol.Protocol.callbackHandler(byteArray) { code, rx ->
            when (code) {
                Protocol.RX_0X01 -> {
                    for (i in 0 until rx.data.size / 2) {
                        val index = rx.data.readInt8(offset = i * 2)
                        val status = rx.data.readInt8(offset = i * 2 + 1)
                        axis[index] = status == 1
                    }
                }

                Protocol.RX_0X02 -> {
                    for (i in 0 until rx.data.size / 2) {
                        val index = rx.data.readInt8(offset = i * 2)
                        val status = rx.data.readInt8(offset = i * 2 + 1)
                        gpio[index] = status == 1
                    }
                }
            }
        }
    }

    override fun exceptionHandler(e: Exception) {
        "Serial Exception: ${e.message}".loge()
    }

    companion object {
        val instance: SerialPortHelper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { SerialPortHelper() }
    }
}