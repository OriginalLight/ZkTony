package com.zktony.www.manager

import com.zktony.core.ext.logi
import com.zktony.serialport.SerialConfig
import com.zktony.serialport.SerialHelpers
import com.zktony.www.manager.protocol.V1
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
    private val _send = MutableStateFlow(V1())
    val send = _send.asStateFlow()
    private val _received = MutableStateFlow(V1())
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
                        val v1 = V1(data)
                        if (v1.cmd == 2) {
                            _received.value = v1
                        }
                    }
                }
            }
            launch {
                while (true) {
                    delay(1000L)
                    helpers.sendHex(4, V1.QUERY_HEX)
                }
            }
        }
    }

    fun init() {
        scope.launch {
            "串口管理器初始化完成！！！".logi()
        }
    }

    /**
     * 发送指令
     * @param v1 [V1] 指令
     */
    fun send(v1: V1) {
        scope.launch {
            scope.launch {
                _send.value = v1
                helpers.sendHex(4, v1.genHex())
            }
        }
    }
}