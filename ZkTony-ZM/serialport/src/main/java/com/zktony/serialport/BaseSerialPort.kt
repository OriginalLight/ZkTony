package com.zktony.serialport

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.zktony.serialport.util.Logger
import com.zktony.serialport.util.SerialDataUtils.hexToByteArr
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.InvalidParameterException

abstract class BaseSerialPort(sPort: String, iBaudRate: Int) : SerialPortHelper(sPort, iBaudRate) {

    private var handler: Handler? = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val recData = msg.obj as String
            onDataBack(recData)
        }
    }

    abstract fun onDataBack(data: String)

    override fun onDataReceived(recData: String) {
        val message = Message()
        message.obj = recData
        handler!!.sendMessage(message)
    }

    override fun close() {
        super.close()
        handler?.let { handler = null }
    }

    /**
     * Open serial port
     *
     * @return 0:success
     * -1: no serial port read/write permission!
     * -2: unknown error!
     * -3: the parameter is wrong!
     * -4: other error!
     */
    fun openSerial(): Int {
        return try {
            super.open()
            Logger.instance.i(TAG, "Open the serial port successfully")
            0
        } catch (e: SecurityException) {
            Logger.instance.e(TAG, "Failed to open the serial port: no serial port read/write permission!")
            -1
        } catch (e: IOException) {
            Logger.instance.e(TAG, "Failed to open serial port: unknown error!")
            -2
        } catch (e: InvalidParameterException) {
            Logger.instance.e(TAG, "Failed to open the serial port: the parameter is wrong!")
            -3
        } catch (e: Exception) {
            Logger.instance.e(TAG, "Failed to open the serial port: other error!")
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
        val bOutArray = hexToByteArr(hex)
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