package com.zktony.www.manager

import com.zktony.core.ext.logi
import com.zktony.serialport.SerialHelper
import com.zktony.serialport.serialConfig
import com.zktony.www.manager.protocol.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author: 刘贺贺
 * @date: 2023-01-03 11:51
 */
class SerialManager {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val _send = MutableStateFlow(Protocol())
    val send = _send.asStateFlow()
    private val _received = MutableStateFlow(Protocol())
    val received = _received.asStateFlow()

    private val helper by lazy {
        SerialHelper(serialConfig {
            device = "/dev/ttyS4"
            delay = 100L
        })
    }

    init {
        scope.launch {
            launch {
                helper.openDevice()
            }
            launch {
                helper.callback = {
                    val protocol = it.toProtocol()
                    if (protocol.cmd == 2) {
                        _received.value = protocol
                    }
                }
            }
            launch {
                while (true) {
                    delay(1000L)
                    helper.sendHex(Protocol.QUERY_HEX)
                }
            }
        }
    }

    /**
     * 发送指令
     * @param protocol [Protocol] 指令
     */
    fun send(protocol: Protocol) {
        _send.value = protocol
        helper.sendHex(protocol.toHex())
    }

    fun initializer() {
        "串口管理器初始化完成！！！".logi()
    }
}