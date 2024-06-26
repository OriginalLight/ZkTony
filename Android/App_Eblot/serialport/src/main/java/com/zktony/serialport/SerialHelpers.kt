package com.zktony.serialport

import android.util.Log
import com.zktony.serialport.config.SerialConfig
import java.util.concurrent.ConcurrentHashMap

class SerialHelpers {
    private val serialMap: MutableMap<Int, SerialHelper> = ConcurrentHashMap()
    var callback: (Int, String) -> Unit = { _, _ -> }

    @Synchronized
    fun init(config: SerialConfig): Int {
        val serial = serialMap[config.index]
        if (serial != null && serial.isOpen) {
            return 1
        }

        val serialHelper = SerialHelper(config)
        serialHelper.callback = { callback(config.index, it) }
        val openStatus = serialHelper.openDevice()
        if (openStatus == 0) {
            serialMap[config.index] = serialHelper
        }
        return openStatus
    }

    fun init(vararg configs: SerialConfig): Int {
        var status = 0
        for (config in configs) {
            status = init(config)
            if (status != 0) {
                break
            }
        }
        return status
    }

    /**
     * 串口是否已经打开
     * Serial port status (open/close)
     */
    fun isOpen(index: Int): Boolean {
        val currentSerial = serialMap[index]
        return currentSerial != null && currentSerial.isOpen
    }

    /**
     * Close the serial port
     */
    fun close(index: Int) {
        val currentSerial = serialMap[index]
        if (currentSerial != null) {
            currentSerial.close()
            serialMap.remove(index)
        } else {
            Log.e(TAG, "The serial port is closed or not initialized")
        }
    }

    /**
     * send data
     *
     * @param index
     * @param hex
     */
    fun sendHex(index: Int, hex: String) {
        val currentSerial = serialMap[index]
        if (currentSerial != null && currentSerial.isOpen) {
            currentSerial.sendHex(hex)
        } else {
            Log.e(TAG, "The serial port is closed or not initialized")
        }
    }

    /**
     *  send data
     *
     *  @param index
     *  @param byteArray
     */
    fun sendByte(index: Int, byteArray: ByteArray) {
        val currentSerial = serialMap[index]
        if (currentSerial != null && currentSerial.isOpen) {
            currentSerial.sendByteArray(byteArray)
        } else {
            Log.e(TAG, "The serial port is closed or not initialized")
        }
    }

    /**
     *  send data
     *
     *  @param index
     *  @param text
     */
    fun sendText(index: Int, text: String) {
        val currentSerial = serialMap[index]
        if (currentSerial != null && currentSerial.isOpen) {
            currentSerial.sendText(text)
        } else {
            Log.e(TAG, "The serial port is closed or not initialized")
        }
    }

    companion object {
        private const val TAG = "SerialHelpers"
    }
}