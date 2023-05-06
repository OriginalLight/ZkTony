package com.zktony.www.proxy

import com.zktony.core.ext.logi
import com.zktony.serialport.SerialHelper
import com.zktony.serialport.config.Protocol
import com.zktony.serialport.config.serialConfig
import com.zktony.serialport.protocol.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author: 刘贺贺
 * @date: 2023-01-03 11:51
 */
class SerialProxy {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val _send = MutableStateFlow(V0())
    val send = _send.asStateFlow()
    private val _received = MutableStateFlow(V0())
    val received = _received.asStateFlow()

    private val helper by lazy {
        SerialHelper(serialConfig {
            device = "/dev/ttyS4"
            delay = 100L
            protocol = Protocol.V0
        })
    }

    init {
        scope.launch {
            launch {
                helper.openDevice()
            }
            launch {
                helper.callback = {
                    val v0 = it.toV0()
                    if (v0.cmd == 2) {
                        _received.value = v0
                    }
                }
            }
            launch {
                while (true) {
                    delay(1000L)
                    helper.sendHex(V0.QUERY_HEX)
                }
            }
        }
    }

    /**
     * 发送指令
     * @param v0 [V0] 指令
     */
    fun send(v0: V0) {
        _send.value = v0
        helper.sendHex(v0.toHex())
    }

    fun initializer() {
        "SerialProxy initializer".logi()
    }
}