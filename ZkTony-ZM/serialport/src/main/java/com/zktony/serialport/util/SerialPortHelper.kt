package com.zktony.serialport.util

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.text.TextUtils
import com.zktony.serialport.core.SerialPort
import com.zktony.serialport.listener.OnSerialDataListener
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.InvalidParameterException

/**
 * Serial port auxiliary tool class
 */
abstract class SerialPortHelper {
    private var mSerialPort: SerialPort? = null
    private var mOutputStream: OutputStream? = null
    private var mInputStream: InputStream? = null
    private var mReadThread: ReadThread? = null
    var isOpen = false
        private set
    private var iDelay = 100
    private var iGap = 30
    var port //Serial port
            : String? = null
    var baudRate //baud rate
            = 0
    var stopBits = 1 //StopBits，1 or 2  （default 1）
    var dataBits = 8 // DataBits，5 ~ 8  （default 8）
    var parity = 0 //Parity，0 None（default）； 1 Odd； 2 Even
    var flowCon = 0 //FlowCon
    private var mWorkHandler: Handler? = null
    private var mHandlerThread: HandlerThread? = null
    private var mSerialDataListener: OnSerialDataListener? = null
    private var mFullData = ""

    constructor()
    constructor(sPort: String?, iBaudRate: Int) {
        port = sPort
        baudRate = iBaudRate
    }

    fun setSerialDataListener(serialDataListene: OnSerialDataListener?) {
        mSerialDataListener = serialDataListene
    }

    @Throws(SecurityException::class, IOException::class, InvalidParameterException::class)
    fun open() {
        mSerialPort = port?.let { File(it) }
            ?.let { SerialPort(it, baudRate, stopBits, dataBits, parity, flowCon, 0) }
        mOutputStream = mSerialPort!!.outputStream
        mInputStream = mSerialPort!!.inputStream
        mReadThread = ReadThread()
        mReadThread!!.start()
        mHandlerThread = HandlerThread("handlerThread")
        mHandlerThread!!.start()
        mWorkHandler = object : Handler(mHandlerThread!!.looper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val sendData = msg.obj as ByteArray
                send(sendData)
                try {
                    Thread.sleep(iDelay.toLong())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        isOpen = true
    }

    open fun close() {
        try {
            if (mInputStream != null) {
                mInputStream!!.close()
            }
            if (mOutputStream != null) {
                mOutputStream!!.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (mReadThread != null) {
            mReadThread!!.interrupt()
        }
        if (mSerialPort != null) {
            mSerialPort!!.close()
            mSerialPort = null
        }
        if (mHandlerThread != null) {
            mHandlerThread!!.quit()
            mHandlerThread = null
        }
        isOpen = false
    }

    protected fun addWaitMessage(msg: Message) {
        mWorkHandler!!.sendMessage(msg)
    }

    private fun send(bOutArray: ByteArray) {
        if (mWorkHandler == null) {
            return
        }
        if (!isOpen) {
            return
        }
        try {
            mOutputStream!!.write(bOutArray)
            if (mSerialDataListener != null) {
                val hexString = SerialDataUtils.byteArrToHex(bOutArray).trim { it <= ' ' }
                mSerialDataListener!!.onSend(hexString)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun parseData(tempData: String) {
        if (tempData == TAG_END && TextUtils.isEmpty(mFullData)) {
            return
        }
        if (tempData == TAG_END && !TextUtils.isEmpty(mFullData)) {
            val trimData = mFullData.trim { it <= ' ' }.replace(" ".toRegex(), "")
            if (mSerialDataListener != null) {
                mSerialDataListener!!.onReceiveFullData(trimData)
            }
            onDataReceived(trimData)
            mFullData = ""
            return
        }
        mFullData += tempData
    }

    protected abstract fun onDataReceived(ComRecData: String)
    fun setDelay(delay: Int) {
        iDelay = delay
    }

    fun setGap(gap: Int) {
        iGap = gap
    }

    private inner class ReadThread : Thread() {
        override fun run() {
            super.run()
            while (!isInterrupted) {
                try {
                    if (mInputStream == null) return
                    var buffer: ByteArray
                    val im = mInputStream!!.available()
                    if (im > 0) {
                        buffer = ByteArray(im)
                        mInputStream!!.read(buffer)
                        val hexString = SerialDataUtils.byteArrToHex(buffer).trim { it <= ' ' }
                        if (mSerialDataListener != null) {
                            mSerialDataListener!!.onReceive(hexString)
                        }
                        parseData(hexString)
                    } else {
                        parseData(TAG_END)
                    }
                    try {
                        sleep(iGap.toLong())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    return
                }
            }
        }
    }

    companion object {
        private const val TAG_END = "0D0A"
    }
}