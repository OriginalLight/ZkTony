package com.zktony.android.utils

import com.zktony.serialport.AbstractSerialHelper
import com.zktony.serialport.command.Protocol
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.readInt8
import java.util.concurrent.CopyOnWriteArrayList

class SerialPortHelper : AbstractSerialHelper(
    SerialConfig(
        device = "/dev/ttyS4"
    )
) {

    val protocol = Protocol()

    val axis: CopyOnWriteArrayList<Boolean> = CopyOnWriteArrayList<Boolean>().apply {
        repeat(16) { add(false) }
    }
    val gpio: CopyOnWriteArrayList<Boolean> = CopyOnWriteArrayList<Boolean>().apply {
        repeat(16) { add(false) }
    }

    override fun callbackHandler(byteArray: ByteArray) {
        protocol.callbackHandler(byteArray) { code, rx ->
            when (code) {
                Protocol.AXIS -> {
                    for (i in 0 until rx.data.size / 2) {
                        val index = rx.data.readInt8(offset = i * 2)
                        val status = rx.data.readInt8(offset = i * 2 + 1)
                        axis[index] = status == 1
                    }
                }

                Protocol.GPIO -> {
                    for (i in 0 until rx.data.size / 2) {
                        val index = rx.data.readInt8(offset = i * 2)
                        val status = rx.data.readInt8(offset = i * 2 + 1)
                        gpio[index] = status == 1
                    }
                }
            }
        }
    }

    companion object {
        val instance: SerialPortHelper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { SerialPortHelper() }
    }
}