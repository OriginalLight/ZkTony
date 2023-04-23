package com.zktony.www.manager

import com.zktony.core.ext.logi
import com.zktony.serialport.SerialConfig
import com.zktony.serialport.SerialHelpers
import com.zktony.www.manager.protocol.Protocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val helpers by lazy { SerialHelpers() }

    init {
        scope.launch {
            launch {
                helpers.init(
                    SerialConfig(
                        index = 4,
                        device = "/dev/ttyS4",
                        delay = 100L
                    )
                )
            }
            launch {
                helpers.callback = { index, data ->
                    if (index == 4) {
                        val protocol = Protocol(data)
                        if (protocol.cmd == 2) {
                            _received.value = protocol
                        }
                    }
                }
            }
            launch {
                while (true) {
                    delay(1000L)
                    helpers.sendHex(4, Protocol.QUERY_HEX)
                }
            }
        }
    }

    /**
     * 发送指令
     * @param protocol [Protocol] 指令
     */
    fun send(protocol: Protocol) {
        scope.launch {
            scope.launch {
                _send.value = protocol
                helpers.sendHex(4, protocol.genHex())
            }
        }
    }

    fun initializer() {
        "串口管理器初始化完成！！！".logi()
    }
}