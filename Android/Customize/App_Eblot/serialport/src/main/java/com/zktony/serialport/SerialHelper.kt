package com.zktony.serialport

import android.os.Message
import android.util.Log
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.hexStringToByteArray
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.InvalidParameterException

/**
 * @author: 刘贺贺
 * @date: 2022-12-08 14:39
 */
class SerialHelper(config: SerialConfig) : AbstractSerialHelper(config) {

    /**
     * Open the serial port
     */
    fun openDevice(): Int {
        return try {
            open()
            Log.i(TAG, "Open the serial port successfully")
            0
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to open the serial port: no serial port read/write permission!")
            -1
        } catch (e: IOException) {
            Log.e(TAG, "Failed to open serial port: unknown error!")
            -2
        } catch (e: InvalidParameterException) {
            Log.e(TAG, "Failed to open the serial port: the parameter is wrong!")
            -3
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open the serial port: other error!")
            -4
        }
    }

    /**
     * Send HEX data
     *
     * @param sHex hex data
     */
    fun sendHex(sHex: String) {
        val hex = sHex.trim { it <= ' ' }.replace(" ", "")
        val bOutArray = hex.hexStringToByteArray()
        val msg = Message.obtain()
        msg.obj = bOutArray
        addWaitMessage(msg)
    }

    /**
     * Send string data
     *
     * @param sText string data
     */
    fun sendText(sText: String) {
        val bOutArray: ByteArray
        try {
            bOutArray = sText.toByteArray()
            val msg = Message.obtain()
            msg.obj = bOutArray
            addWaitMessage(msg)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    /**
     * Send byte data
     *
     * @param bytes byte data
     */
    fun sendByteArray(bytes: ByteArray) {
        val msg = Message.obtain()
        msg.obj = bytes
        addWaitMessage(msg)
    }

    companion object {
        private const val TAG = "SerialHelper"
    }

}