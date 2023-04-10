package com.zktony.www.manager

import com.zktony.serialport.SerialMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SerialManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val serialMap by lazy { SerialMap() }

    init {
        scope.launch {
            launch {

            }
        }
    }

    /**
     * 发送Hex
     * @param serial 串口
     * @param hex 命令
     */
    fun sendHex(index: Int, hex: String) {
        scope.launch {
            serialMap.sendHex(index, hex)
        }
    }

    /**
     * 发送Text
     * @param serial 串口
     * @param text 命令
     */
    fun sendText(index: Int, text: String) {
        scope.launch {
            serialMap.sendText(index, text)
        }
    }
}
