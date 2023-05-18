package com.zktony.android.core

import com.zktony.core.ext.logi
import com.zktony.serialport.SerialHelper
import com.zktony.serialport.command.protocol
import com.zktony.serialport.config.serialConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Vector

class SerialPort {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private val helper by lazy {
        SerialHelper(serialConfig {})
    }

    private val _callback = MutableStateFlow(byteArrayOf())

    val callback = _callback.asStateFlow()
    val vector = Vector<Int>(16).apply {
        for (i in 0..15) {
            add(0)
        }
    }

    init {
        scope.launch {
            helper.openDevice()
            helper.callback = { hexHandler(it) }
        }
    }

    /**
     * 发送命令
     *
     * @param bytes ByteArray
     * @return Unit
     */
    fun sendByteArray(bytes: ByteArray) {
        helper.sendByteArray(bytes = bytes)
    }

    private fun hexHandler(bytes: ByteArray) {
        _callback.value = bytes
        val rec = protocol(bytes)
        if (rec.addr == 0x02.toByte()) {
            when (rec.fn) {
                0x01.toByte() -> {
                    val index = rec.data[0].toInt()
                    val value = rec.data[1].toInt()
                    vector[index] = value
                }
            }
        }
    }

    /**
     * 初始化
     */
    fun initializer() {
        "SerialPort initializer".logi()
    }

}
