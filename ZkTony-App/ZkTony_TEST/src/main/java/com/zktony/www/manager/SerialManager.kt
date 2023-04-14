package com.zktony.www.manager

import com.zktony.core.ext.logi
import com.zktony.serialport.SerialHelpers
import kotlinx.coroutines.*

class SerialManager {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val helpers by lazy { SerialHelpers() }

    init {
        scope.launch {
            launch {

            }
        }
    }

    fun init() {
        scope.launch {
            "串口管理器初始化完成！！！".logi()
        }
    }

    /**
     * 发送Hex
     * @param hex 命令
     */
    fun sendHex(index: Int, hex: String) {
        scope.launch {
            helpers.sendHex(index, hex)
        }
    }

    /**
     * 发送Text
     * @param text 命令
     */
    fun sendText(index: Int, text: String) {
        scope.launch {
            helpers.sendText(index, text)
        }
    }
}
