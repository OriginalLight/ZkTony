package com.zktony.www.helper

import com.zktony.serialport.SerialHelper
import com.zktony.serialport.config.serialConfig
import kotlinx.coroutines.*

class SerialHelper {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private val helper by lazy { SerialHelper(serialConfig {}) }

    init {
        scope.launch {
            helper.openDevice()
        }
    }

}
