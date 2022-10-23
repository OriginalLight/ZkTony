package com.zktony.serialport

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.zktony.serialport.util.Logger
import com.zktony.serialport.util.SerialDataUtils.hexToByteArr
import com.zktony.serialport.util.SerialPortHelper
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.InvalidParameterException

abstract class BaseSerialPort : SerialPortHelper {
    @SuppressLint("HandlerLeak")
    private var mHandler: Handler? = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val recData = msg.obj as String
            onDataBack(recData)
        }
    }

    constructor() : super() {}
    constructor(sPort: String?, iBaudRate: Int) : super(sPort, iBaudRate) {}

    abstract fun onDataBack(data: String)
    override fun onDataReceived(ComRecData: String) {
        val message = Message()
        message.obj = ComRecData
        mHandler!!.sendMessage(message)
    }

    override fun close() {
        super.close()
        if (mHandler != null) {
            mHandler = null
        }
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
            Logger.instance.e(
                TAG,
                "Failed to open the serial port: no serial port read/write permission!"
            )
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
     * @param sTxt string data
     */
    fun sendTxt(sTxt: String) {
        val bOutArray: ByteArray
        try {
            bOutArray = sTxt.toByteArray(charset("GB18030"))
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

    /**
     * is show log
     *
     * @param isShowLog true=show
     */
    fun setShowLog(isShowLog: Boolean) {
        Logger.SHOW_LOG = isShowLog
    }

    companion object {
        private const val TAG = "BaseSerial"
    }
}