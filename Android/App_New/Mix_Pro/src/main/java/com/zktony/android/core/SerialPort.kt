package com.zktony.android.core

import com.zktony.core.ext.logi
import com.zktony.serialport.SerialHelper
import com.zktony.serialport.config.Protocol
import com.zktony.serialport.config.serialConfig
import com.zktony.serialport.ext.hexFormat
import com.zktony.serialport.ext.hexToInt
import com.zktony.serialport.protocol.toV2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Vector

class SerialPort {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private val helper by lazy {
        SerialHelper(serialConfig {
            crc = true
            protocol = Protocol.V2
        })
    }

    private val _callback = MutableStateFlow<String?>(null)

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
     * 发送Hex
     *
     * @param hex String
     */
    fun sendHex(hex: String) {
        helper.sendHex(hex)
        hex.hexFormat().logi()
    }

    private fun hexHandler(hex: String) {
        _callback.value = hex
        val v2 = hex.toV2()
        if (v2 != null && v2.addr == "02") {
            when (v2.fn) {
                "01" -> {
                    val index = v2.data.substring(0, 2).hexToInt()
                    val value = v2.data.substring(2, 4).hexToInt()
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
