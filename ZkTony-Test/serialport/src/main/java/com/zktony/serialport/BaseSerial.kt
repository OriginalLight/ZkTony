package com.zktony.serialport

import android.os.Message
import android.util.Log
import com.zktony.serialport.util.Serial
import com.zktony.serialport.util.SerialDataUtils
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.InvalidParameterException

/**
 * @author: 刘贺贺
 * @date: 2022-12-08 14:39
 */
abstract class BaseSerial(sPort: Serial, iBaudRate: Int) : SerialHelper(sPort, iBaudRate) {

    /**
     * Open the serial port
     */
    fun openSerial(): Int {
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
        val hex = sHex.trim { it <= ' ' }.replace(" ".toRegex(), "")
        val bOutArray = SerialDataUtils.hexToByteArr(hex)
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
            bOutArray = sText.toByteArray(charset("GB18030"))
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
     * @param bOutArray byte data
     */
    fun sendByteArray(bOutArray: ByteArray) {
        val msg = Message.obtain()
        msg.obj = bOutArray
        addWaitMessage(msg)
    }

    companion object {
        private const val TAG = "BaseSerial"
    }

}