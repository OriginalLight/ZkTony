package com.zktony.www.manager

import com.zktony.core.ext.logi
import com.zktony.serialport.SerialConfig
import com.zktony.serialport.SerialMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-01-03 11:51
 */
class SerialManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _callback = MutableStateFlow<String?>(null)
    val callback = _callback.asStateFlow()

    private val serialMap by lazy { SerialMap() }

    init {
        scope.launch {
            launch {
                serialMap.init(SerialConfig(
                    index = 4,
                    device = "/dev/ttyS4",
                    delay = 100L
                ))
            }
            launch {
                serialMap.callback = { index, data ->
                    if (index == 4) {
                        _callback.value = data
                    }
                }
            }
        }
    }

    fun send(hex: String) {
        serialMap.sendHex(4, hex)
    }

    fun test() {
        scope.launch {
            "SerialManager test".logi()
        }
    }
}