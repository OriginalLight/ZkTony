package com.zktony.serialport

import android.util.Log
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.ascii2ByteArray
import com.zktony.serialport.ext.hex2ByteArray
import java.io.IOException
import java.security.InvalidParameterException

/**
 * @author: 刘贺贺
 * @date: 2022-12-08 14:39
 */
abstract class AbstractSerialPort(config: SerialConfig) : AbstractSerial() {

    init {
        // Open the serial port when the class is initialized
        openDevice(config)
    }

    /**
     * Open the serial port
     */
    private fun openDevice(config: SerialConfig): Int {
        return try {
            open(config)
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
     * Send byte data
     *
     * @param bytes byte data
     */
    fun sendByteArray(bytes: ByteArray) {
        if (bytes.isEmpty()) {
            Log.w(TAG, "The byte array to be sent is empty")
            return
        }
        addByteArrayToQueue(bytes)
    }

    /**
     * Send hex string
     *
     * @param hex String
     */
    fun sendHexString(hex: String) {
        if (hex.isEmpty() || hex.isBlank()) {
            Log.w(TAG, "The hex to be sent is empty or blank")
            return
        }
        sendByteArray(hex.hex2ByteArray())
    }

    /**
     * Send ascii string
     *
     * @param ascii String
     */
    fun sendAsciiString(ascii: String) {
        if (ascii.isEmpty() || ascii.isBlank()) {
            Log.w(TAG, "The ascii to be sent is empty or blank")
            return
        }
        sendByteArray(ascii.ascii2ByteArray(true))
    }

    /**
     * Register the callback function
     */
    fun registerCallback(key: String, callback: (ByteArray) -> Unit) {
        if (callbacks.containsKey(key)) {
            Log.w(TAG, "The key of the callback function already exists, the old key will be overwritten")
            callbacks[key] = callback
        }
    }

    /**
     * Unregister the callback function
     */
    fun unregisterCallback(key: String) {
        if (!callbacks.containsKey(key)) {
            Log.w(TAG, "The key of the callback function does not exist")
            return
        }
        callbacks.remove(key)
    }

    companion object {
        private const val TAG = "AbstractSerialHelper"
    }

}