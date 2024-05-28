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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Serial port helper class (abstract class)
 */
abstract class AbstractSerialPort {

    private val executor = Executors.newFixedThreadPool(2)
    private var serialPort: SerialPort? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var config: SerialConfig = SerialConfig()
    private var isOpen: Boolean = false
    private val buffer = ByteArrayOutputStream()
    private val byteArrayQueue = LinkedBlockingQueue<ByteArray>()

    val callbacks : MutableMap<String, (ByteArray) -> Unit> = ConcurrentHashMap()

    /**
     * Open the serial port
     */
    @Throws(SecurityException::class, IOException::class, InvalidParameterException::class)
    fun open(config: SerialConfig) {
        this.config = config
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
            ex.printStackTrace()
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
                ex.printStackTrace()
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
                    callbacks.forEach { (key, callback) ->
                        Log.i(config.device, "Callback Invoke: $key")
                        callback.invoke((buffer.toByteArray()))
                    }
                    if (config.log) {
                        Log.i(config.device, "RX: ${buffer.toByteArray().toHexString()}")
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
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
                        Thread.sleep(10L)
                    } catch (ex: InterruptedException) {
                        ex.printStackTrace()
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
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
                    val message = byteArrayQueue.poll(config.delay, TimeUnit.MILLISECONDS)
                    if (message != null) {
                        send(message)
                        if (config.log) {
                            Log.i(config.device, "TX: ${message.toHexString()}")
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }
}