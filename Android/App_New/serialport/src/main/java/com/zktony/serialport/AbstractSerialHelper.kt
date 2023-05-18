package com.zktony.serialport

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.core.SerialPort
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.InvalidParameterException

/**
 * Serial port auxiliary tool class
 */
abstract class AbstractSerialHelper(serialConfig: SerialConfig) {
    /**
     * Serial port
     */
    private val config = serialConfig
    private var serialPort: SerialPort? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    var isOpen: Boolean = false

    /**
     * Thread for receiving data
     */
    private var readThread: ReadThread? = null
    private var handler: Handler? = null
    private var handlerThread: HandlerThread? = null

    private var cache: ByteArray = byteArrayOf()
    var callback: (ByteArray) -> Unit = { }


    @Throws(SecurityException::class, IOException::class, InvalidParameterException::class)
    fun open() {
        serialPort = SerialPort(
            device = File(config.device),
            baudRate = config.baudRate,
            stopBits = config.stopBits,
            dataBits = config.dataBits,
            parity = config.parity,
            flowCon = config.flowCon,
            flags = config.flags,
        )
        serialPort?.let {
            outputStream = it.outputStream
            inputStream = it.inputStream
        }
        readThread = ReadThread()
        readThread?.start()

        handlerThread = HandlerThread("handlerThread")
        handlerThread?.let {
            it.start()
            handler = object : Handler(it.looper) {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    send(msg.obj as ByteArray)
                    try {
                        Thread.sleep(config.delay)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        isOpen = true
    }

    fun addWaitMessage(msg: Message) {
        handler?.sendMessage(msg)
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
        handlerThread?.interrupt()
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

    /**
     * data processing
     */
    private fun dataProcess(byteArray: ByteArray) {
        if (byteArray.isEmpty()) {
            if (cache.isNotEmpty()) {
                callback.invoke(cache)
                cache = byteArrayOf()
            }
            return
        }
        cache.plus(byteArray)
    }

    /**
     * Thread for receiving data
     */
    private inner class ReadThread : Thread() {
        override fun run() {
            super.run()
            while (!isInterrupted) {
                try {
                    val input = inputStream?.available()
                    input?.let {
                        if (it > 0) {
                            val buffer = ByteArray(it)
                            inputStream?.read(buffer)
                            dataProcess(buffer)
                        } else {
                            dataProcess(byteArrayOf())
                        }
                    }
                    try {
                        sleep(1L)
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
}