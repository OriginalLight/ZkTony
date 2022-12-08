package com.zktony.serialport

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.zktony.serialport.core.SerialPort
import com.zktony.serialport.util.Logger
import com.zktony.serialport.util.SerialDataUtils
import com.zktony.serialport.util.SerialDataUtils.byteArrToHex
import java.io.*
import java.security.InvalidParameterException

/**
 * Serial port auxiliary tool class
 */
abstract class SerialPortHelper(sPort: String, iBaudRate: Int) {
    private lateinit var serialPort: SerialPort
    private lateinit var outputStream: OutputStream
    private lateinit var inputStream: InputStream
    private lateinit var readThread: ReadThread
    private lateinit var workHandler: Handler
    private lateinit var handlerThread: HandlerThread
    private var fullData = ""
    private val sendDelay = 100L
    private val readDelay = 30L
    private val port = sPort
    private val baudRate = iBaudRate
    var onDataReceived: (String) -> Unit = { _ -> }
    var stopBits = 1 //StopBits，1 or 2  （default 1）
    var dataBits = 8 // DataBits，5 ~ 8  （default 8）
    var parity = 0 //Parity，0 None（default）； 1 Odd； 2 Even
    var flowCon = 0 //FlowCon
    var isOpen = false


    fun openSerial(): Int {
        return try {
            open()
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


    @Throws(SecurityException::class, IOException::class, InvalidParameterException::class)
    fun open() {
        serialPort = SerialPort(File(port), baudRate, stopBits, dataBits, parity, flowCon, 0)
        outputStream = serialPort.outputStream
        inputStream = serialPort.inputStream
        readThread = ReadThread()
        readThread.start()
        handlerThread = HandlerThread("handlerThread")
        handlerThread.start()
        workHandler = object : Handler(handlerThread.looper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                send(msg.obj as ByteArray)
                try {
                    Thread.sleep(sendDelay)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        isOpen = true
    }

    open fun close() {
        try {
            if (::inputStream.isInitialized) {
                inputStream.close()
            }
            if (::outputStream.isInitialized) {
                outputStream.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (::serialPort.isInitialized) {
            serialPort.close()
        }
        if (::readThread.isInitialized) {
            readThread.interrupt()
        }
        if (::handlerThread.isInitialized) {
            handlerThread.quit()
        }
        isOpen = false
    }

    private fun addWaitMessage(msg: Message) {
        workHandler.sendMessage(msg)
    }

    private fun send(bOutArray: ByteArray) {
        if (!isOpen) return
        try {
            outputStream.write(bOutArray)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun parseData(tempData: String) {
        if (tempData == TAG_END) {
            if (fullData.isNotEmpty()) {
                onDataReceived.invoke(fullData.trim { it <= ' ' }.replace(" ".toRegex(), ""))
                fullData = ""
            }
            return
        }
        fullData += tempData
    }

    private inner class ReadThread : Thread() {
        override fun run() {
            super.run()
            while (!isInterrupted) {
                try {
                    val im = inputStream.available()
                    if (im > 0) {
                        val buffer = ByteArray(im)
                        inputStream.read(buffer)
                        val hexString = byteArrToHex(buffer).trim { it <= ' ' }
                        parseData(hexString)
                    } else {
                        parseData(TAG_END)
                    }
                    try {
                        sleep(readDelay)
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
        private const val TAG = "SerialPortHelper"
        private const val TAG_END = "0D0A"
    }
}