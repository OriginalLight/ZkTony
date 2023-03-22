package com.zktony.www.manager

import com.zktony.serialport.MutableSerial
import com.zktony.serialport.util.Serial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SerialManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

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
    fun sendHex(serial: Serial, hex: String) {
        scope.launch {
            MutableSerial.instance.sendHex(serial, hex)
            //Logger.e(msg = "${serial.value} sendHex: ${hex.hexFormat()}")
        }
    }

    /**
     * 发送Text
     * @param serial 串口
     * @param text 命令
     */
    fun sendText(serial: Serial, text: String) {
        scope.launch {
            MutableSerial.instance.sendText(serial, text)
            //Logger.e(msg = "${serialPort.value} sendText: $text")
        }
    }


    companion object {
        @JvmStatic
        val instance: SerialManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SerialManager()
        }
    }
}
