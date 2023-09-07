package com.zktony.serialport

import android.util.Log
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.core.SerialPort
import com.zktony.serialport.ext.toHexString
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.InvalidParameterException
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Serial port helper class (abstract class)
 */
abstract class AbstractSerial {

    private val executor = Executors.newFixedThreadPool(2)
    private var serialPort: SerialPort? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var delay: Long = 10L
    private var isOpen: Boolean = false
    private val buffer = ByteArrayOutputStream()
    private val messageQueue = LinkedBlockingQueue<ByteArray>()

    /**
     * Callback handler
     */
    @Throws(Exception::class)
    abstract fun callbackHandler(byteArray: ByteArray)

    /**
     * Exception handler
     */
    abstract fun exceptionHandler(e: Exception)

    /**
     * Open the serial port
     */
    @Throws(SecurityException::class, IOException::class, InvalidParameterException::class)
    fun open(config: SerialConfig) {
        delay = config.delay
        serialPort = SerialPort(
            device = File(config.device),
            baudRate = config.baudRate,
            stopBits = config.stopBits,
            dataBits = config.dataBits,
            parity = config.parity,
            flowCon = config.flowCon,
            flags = config.flags
        )
        serialPort?.let {
            outputStream = it.outputStream
            inputStream = it.inputStream
        }

        isOpen = true
        startMessageReceiver()
        startMessageSender()
    }

    /**
     * Close the serial port
     */
    open fun close() {
        try {
            inputStream?.close()
            outputStream?.close()
            serialPort?.close()
            executor.shutdownNow()
            isOpen = false
        } catch (ex: IOException) {
            exceptionHandler(ex)
        }
    }

    /**
     * Add message to the message queue
     */
    fun addWaitMessage(byteArray: ByteArray) {
        messageQueue.add(byteArray)
    }

    /**
     * Send data
     */
    private fun send(bOutArray: ByteArray) {
        if (isOpen) {
            try {
                outputStream?.write(bOutArray)
            } catch (ex: IOException) {
                exceptionHandler(ex)
            }
        }
    }

    /**
     * data processing
     */
    private fun dataProcess(byteArray: ByteArray?) {
        if (byteArray == null) {
            if (buffer.size() > 0) {
                try {
                    callbackHandler(buffer.toByteArray())
                } catch (ex: Exception) {
                    exceptionHandler(ex)
                } finally {
                    buffer.reset()
                }
            }
        } else {
            buffer.write(byteArray)
        }
    }

    /**
     * Thread for receiving data
     */
    private fun startMessageReceiver() {
        executor.execute {
            val buffer = ByteArray(1024)
            while (isOpen) {
                try {
                    val bytesRead = inputStream?.read(buffer)
                    if (bytesRead != null && bytesRead > 0) {
                        dataProcess(buffer.copyOfRange(0, bytesRead))
                    } else {
                        dataProcess(null)
                    }

                    try {
                        Thread.sleep(4L)
                    } catch (ex: InterruptedException) {
                        exceptionHandler(ex)
                    }
                } catch (e: Exception) {
                    exceptionHandler(e)
                }
            }
        }
    }

    /**
     * Thread for sending data
     */
    private fun startMessageSender() {
        executor.execute {
            while (isOpen) {
                try {
                    val message = messageQueue.poll(10L, TimeUnit.MILLISECONDS)
                    Log.d("AbstractSerial", "Send message: ${message.toHexString()}")
                    if (message != null && message.isNotEmpty()) {
                        send(message)
                        Thread.sleep(delay)
                    }
                } catch (ex: Exception) {
                    exceptionHandler(ex)
                }
            }
        }
    }
}