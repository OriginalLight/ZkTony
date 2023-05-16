package com.zktony.serialport

import android.os.*
import com.zktony.serialport.config.Protocol
import com.zktony.serialport.config.SerialConfig
import com.zktony.serialport.core.SerialPort
import com.zktony.serialport.ext.*
import java.io.*
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

    private var cache: String = ""

    var callback: (String) -> Unit = { _ -> }


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
            shell = config.shell
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
                if (config.crc) {
                    val buffer = bOutArray.slice(0 until bOutArray.size - 3).toByteArray()
                    val end = bOutArray.slice(bOutArray.size - 1 until bOutArray.size).toByteArray()
                    val crc = buffer.crc16()
                    val crcArray = buffer + crc + end
                    outputStream?.write(crcArray)
                } else {
                    outputStream?.write(bOutArray)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (!isOpen) return
    }

    /**
     * data processing
     * @param temp
     * @return
     */
    private fun dataProcess(temp: String) {
        if (temp == TAG_END) {
            if (cache.isNotEmpty()) {
                when (config.protocol) {
                    // 转膜仪的不需要分割
                    Protocol.V0 -> {
                        callback.invoke(cache)
                    }
                    // 旧版的下位机需要分割
                    Protocol.V1 -> {
                        cache.splitString("EE", "FFFCFFFF").forEach {
                            if (it.isNotEmpty()) {
                                callback.invoke(it)
                            }
                        }
                    }
                    // 新版的下位机需要分割和crc验证
                    Protocol.V2 -> {
                        cache.splitString("EE", "BB").forEach {
                            if (it.isNotEmpty()) {
                                val hex = it
                                if (config.crc) {
                                    val crc = hex.substring(hex.length - 6, hex.length - 2)
                                    val buffer = hex.substring(0, hex.length - 6)
                                    if (buffer.crc16() == crc) {
                                        callback.invoke(hex)
                                    }
                                } else {
                                    callback.invoke(hex)
                                }
                            }
                        }
                    }
                }
                cache = ""
            }
            return
        }
        cache += temp
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
                            dataProcess(buffer.byteArrayToHexString())
                        } else {
                            dataProcess(TAG_END)
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

    companion object {
        private const val TAG_END = "0D0A"
    }
}