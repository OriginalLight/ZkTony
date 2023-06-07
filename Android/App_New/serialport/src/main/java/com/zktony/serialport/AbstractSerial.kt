package com.zktony.serialport

import android.os.Message
import android.util.Log
import com.zktony.serialport.command.Protocol
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.ext.ascii2ByteArray
import com.zktony.serialport.ext.hex2ByteArray
import java.io.IOException
import java.security.InvalidParameterException

/**
 * @author: 刘贺贺
 * @date: 2022-12-08 14:39
 */
abstract class AbstractSerial : AbstractSerialHelper() {

    init {
        callback = {
            callbackProcess(it) { bytes ->
                byteArrayProcess(bytes)
            }
        }
        exception = {
            it.printStackTrace()
        }
    }

    /**
     * Open the serial port
     */
    fun openDevice(config: SerialConfig): Int {
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
        val msg = Message.obtain()
        msg.obj = bytes
        addWaitMessage(msg)
    }

    /**
     * Send hex string
     *
     * @param hex String
     */
    fun sendHexString(hex: String) {
        val msg = Message.obtain()
        msg.obj = hex.hex2ByteArray()
        addWaitMessage(msg)
    }


    /**
     * Send ascii string
     *
     * @param ascii String
     */
    fun sendAsciiString(ascii: String) {
        val msg = Message.obtain()
        msg.obj = ascii.ascii2ByteArray(true)
        addWaitMessage(msg)
    }

    /**
     * Send protocol
     *
     * @param protocol Protocol
     */
    fun sendProtocol(protocol: Protocol) {
        val msg = Message.obtain()
        msg.obj = protocol.toByteArray()
        addWaitMessage(msg)
    }

    /**
     * Callback process
     * 包括分包、crc校验等
     *
     * @param byteArray ByteArray
     */
    abstract fun callbackProcess(byteArray: ByteArray, block: (ByteArray) -> Unit = {})

    /**
     * ByteArray process
     *
     * @param byteArray ByteArray
     */
    abstract fun byteArrayProcess(byteArray: ByteArray)

    companion object {
        private const val TAG = "SerialHelper"
    }

}