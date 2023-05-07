package com.zktony.android.proxy

import com.zktony.core.ext.logi
import com.zktony.serialport.SerialHelper
import com.zktony.serialport.config.serialConfig
import com.zktony.serialport.ext.hexFormat
import com.zktony.serialport.ext.hexToInt
import com.zktony.serialport.protocol.toV2
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

class SerialProxy {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val helper by lazy { SerialHelper(serialConfig {}) }

    private val _callback = MutableStateFlow<String?>(null)

    val callback = _callback.asStateFlow()
    val map: MutableMap<Int, Int> = ConcurrentHashMap()

    init {
        scope.launch {
            launch {
                helper.openDevice()
            }
            launch {
                helper.callback = { hexHandler(it) }
            }
            launch {
                for (i in 0..15) {
                    map[i] = 0
                }
            }
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
            when(v2.fn) {
                "01" -> {
                    val index = v2.data.substring(0, 2).hexToInt()
                    val value = v2.data.substring(2, 4).hexToInt()
                    map[index] = value
                }
            }
        }
    }

    /**
     * 初始化
     */
    fun initializer() {
        "SerialProxy initializer".logi()
    }

}
