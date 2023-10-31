package com.zktony.serialport

import android.util.Log
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.ascii2ByteArray
import com.zktony.serialport.ext.hex2ByteArray
import com.zktony.serialport.lifecycle.SerialResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.security.InvalidParameterException

/**
 * @author: 刘贺贺
 * @date: 2022-12-08 14:39
 */
abstract class AbstractSerialHelper(config: SerialConfig) : AbstractSerial() {

    init {
        // Open the serial port
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
    suspend fun sendByteArray(
        bytes: ByteArray,
        timeOut: Long = 1000L,
        block: ((SerialResult) -> Unit)? = null
    ) {
        if (block == null) {
            addByteArrayToQueue(bytes)
        } else {
            try {
                withTimeout(timeOut) {
                    var ref = ByteArray(0)
                    callbackHandler = { ref = it }
                    addByteArrayToQueue(bytes)
                    while (ref.isEmpty()) {
                        delay(10L)
                    }
                    block(SerialResult.Success(ref))
                }
            } catch (ex: Exception) {
                block(SerialResult.Failure(ex))
            } finally {
                callbackHandler = null
            }
        }
    }

    /**
     * Send hex string
     *
     * @param hex String
     */
    suspend fun sendHexString(
        hex: String,
        timeOut: Long = 1000L,
        block: ((SerialResult) -> Unit)? = null
    ) {
        sendByteArray(hex.hex2ByteArray(), timeOut, block)
    }

    /**
     * Send ascii string
     *
     * @param ascii String
     */
    suspend fun sendAsciiString(
        ascii: String,
        timeOut: Long = 1000L,
        block: ((SerialResult) -> Unit)? = null
    ) {
        sendByteArray(ascii.ascii2ByteArray(true), timeOut, block)
    }


    companion object {
        private const val TAG = "AbstractSerialHelper"
    }

}