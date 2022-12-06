package com.zktony.serialport

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.text.TextUtils
import com.zktony.serialport.core.SerialPort
import com.zktony.serialport.util.SerialDataUtils.byteArrToHex
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.InvalidParameterException

/**
 * Serial port auxiliary tool class
 */
abstract class SerialPortHelper {
    private var serialPort: SerialPort? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var readThread: ReadThread? = null
    var isOpen = false
        private set
    private var iDelay = 30
    private var iGap = 30
    var port: String? = null
    var baudRate = 0
    var stopBits = 1 //StopBits，1 or 2  （default 1）
    var dataBits = 8 // DataBits，5 ~ 8  （default 8）
    var parity = 0 //Parity，0 None（default）； 1 Odd； 2 Even
    var flowCon = 0 //FlowCon
    private var workHandler: Handler? = null
    private var handlerThread: HandlerThread? = null
    private var fullData = ""

    constructor()
    constructor(sPort: String?, iBaudRate: Int) {
        port = sPort
        baudRate = iBaudRate
    }

    @Throws(SecurityException::class, IOException::class, InvalidParameterException::class)
    fun open() {
        serialPort = SerialPort(File(port!!), baudRate, stopBits, dataBits, parity, flowCon, 0)
        outputStream = serialPort!!.outputStream
        inputStream = serialPort!!.inputStream
        readThread = ReadThread()
        readThread!!.start()
        handlerThread = HandlerThread("handlerThread")
        handlerThread!!.start()
        workHandler = object : Handler(handlerThread!!.looper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                send(msg.obj as ByteArray)
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
            inputStream?.close()
            outputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        readThread?.interrupt()
        serialPort?.let {
            it.close()
            serialPort = null
        }
        handlerThread?.let {
            it.quit()
            handlerThread = null
        }
        isOpen = false
    }

    protected fun addWaitMessage(msg: Message) {
        workHandler!!.sendMessage(msg)
    }

    private fun send(bOutArray: ByteArray) {
        if (workHandler == null) return
        if (!isOpen) return
        try {
            outputStream!!.write(bOutArray)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun parseData(tempData: String) {
        if (tempData == TAG_END && TextUtils.isEmpty(fullData)) return
        if (tempData == TAG_END && !TextUtils.isEmpty(fullData)) {
            val trimData = fullData.trim { it <= ' ' }.replace(" ".toRegex(), "")
            onDataReceived(trimData)
            fullData = ""
            return
        }
        fullData += tempData
    }

    protected abstract fun onDataReceived(recData: String)

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
                    if (inputStream == null) return
                    val im = inputStream!!.available()
                    if (im > 0) {
                        val buffer = ByteArray(im)
                        inputStream!!.read(buffer)
                        val hexString = byteArrToHex(buffer).trim { it <= ' ' }
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