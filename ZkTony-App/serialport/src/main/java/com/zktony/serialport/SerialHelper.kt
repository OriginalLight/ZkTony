package com.zktony.serialport

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.zktony.serialport.core.SerialPort
import com.zktony.serialport.util.Serial
import com.zktony.serialport.util.SerialDataUtils.byteArrToHex
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.InvalidParameterException

/**
 * Serial port auxiliary tool class
 */
abstract class SerialHelper(sPort: Serial, iBaudRate: Int) {
    private var serialPort: SerialPort? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var readThread: ReadThread? = null
    private var workHandler: Handler? = null
    private var handlerThread: HandlerThread? = null
    private var readData: String = ""
    private val port: File = File(sPort.device)
    private val baudRate: Int = iBaudRate
    var onDataReceived: (String) -> Unit = { _ -> }
    var stopBits: Int = 1 //StopBits，1 or 2  （default 1）
    var dataBits: Int = 8 // DataBits，5 ~ 8  （default 8）
    var parity: Int = 0 //Parity，0 None（default）； 1 Odd； 2 Even
    var flowCon: Int = 0 //FlowCon
    var isOpen: Boolean = false
    var readDelay: Long = 30L
    var sendDelay: Long = 30L

    @Throws(SecurityException::class, IOException::class, InvalidParameterException::class)
    fun open() {
        serialPort = SerialPort(port, baudRate, stopBits, dataBits, parity, flowCon, 0)
        serialPort?.let {
            outputStream = it.outputStream
            inputStream = it.inputStream
        }
        readThread = ReadThread()
        readThread?.start()
        handlerThread = HandlerThread("handlerThread")
        handlerThread?.let {
            it.start()
            workHandler = object : Handler(it.looper) {
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
        }
        isOpen = true
    }

    fun addWaitMessage(msg: Message) {
        workHandler?.sendMessage(msg)
    }

    open fun close() {
        try {
            inputStream?.close()
            outputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        serialPort?.close()
        readThread?.interrupt()
        handlerThread?.quit()
        isOpen = false
    }

    private fun send(bOutArray: ByteArray) {
        if (isOpen) {
            try {
                outputStream?.write(bOutArray)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (!isOpen) return
    }

    private fun parseData(tempData: String) {
        if (tempData == TAG_END) {
            if (readData.isNotEmpty()) {
                onDataReceived.invoke(readData.trim { it <= ' ' }.replace(" ".toRegex(), ""))
                readData = ""
            }
            return
        }
        readData += tempData
    }

    private inner class ReadThread : Thread() {
        override fun run() {
            super.run()
            while (!isInterrupted) {
                try {
                    val im = inputStream?.available()
                    if (im != null) {
                        if (im > 0) {
                            val buffer = ByteArray(im)
                            inputStream?.read(buffer)
                            val hexString = byteArrToHex(buffer).trim { it <= ' ' }
                            parseData(hexString)
                        } else {
                            parseData(TAG_END)
                        }
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
        private const val TAG_END = "0D0A"
    }
}