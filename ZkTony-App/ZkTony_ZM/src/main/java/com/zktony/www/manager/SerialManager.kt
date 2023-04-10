package com.zktony.www.manager

import com.zktony.core.ext.logi
import com.zktony.serialport.MutableSerial
import com.zktony.serialport.util.Serial
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
    private val _ttys4Flow = MutableStateFlow<String?>(null)
    val ttys4Flow = _ttys4Flow.asStateFlow()

    init {
        scope.launch {
            launch {
                MutableSerial.instance.init(Serial.TTYS4, 115200, 100L, 30L)
            }
            launch {
                MutableSerial.instance.listener = { _, data ->
                    _ttys4Flow.value = data
                }
            }
        }
    }

    fun send(data: String) {
        MutableSerial.instance.sendHex(Serial.TTYS4, data)
    }

    fun test() {
        scope.launch {
            "SerialManager test".logi()
        }
    }

}