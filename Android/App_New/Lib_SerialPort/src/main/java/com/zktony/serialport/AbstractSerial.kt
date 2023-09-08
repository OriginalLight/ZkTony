package com.zktony.serialport

import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.core.SerialPort
import java.io.*
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
    private val byteArrayQueue = LinkedBlockingQueue<ByteArray>()

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
        byteArrayReceiver()
        byteArraySender()
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
    fun addByteArrayToQueue(byteArray: ByteArray) {
        byteArrayQueue.add(byteArray)
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
    private fun byteArrayReceiver() {
        executor.execute {
            val buffer = ByteArray(1024)
            while (isOpen) {
                try {
                    val available = inputStream?.available()
                    if (available != null && available > 0) {
                        val bytesRead = inputStream?.read(buffer)
                        if (bytesRead != null && bytesRead > 0) {
                            dataProcess(buffer.copyOfRange(0, bytesRead))
                        } else {
                            dataProcess(null)
                        }
                    } else {
                        dataProcess(null)
                    }

                    try {
                        Thread.sleep(4L)
                    } catch (ex: InterruptedException) {
                        exceptionHandler(ex)
                    }
                } catch (ex: Exception) {
                    exceptionHandler(ex)
                }
            }
        }
    }

    /**
     * Thread for sending data
     */
    private fun byteArraySender() {
        executor.execute {
            while (isOpen) {
                try {
                    val message = byteArrayQueue.poll(delay, TimeUnit.MILLISECONDS)
                    if (message != null) {
                        send(message)
                    }
                } catch (ex: Exception) {
                    exceptionHandler(ex)
                }
            }
        }
    }
}