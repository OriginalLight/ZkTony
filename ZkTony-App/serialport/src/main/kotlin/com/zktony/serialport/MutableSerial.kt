package com.zktony.serialport

import android.util.Log
import com.zktony.serialport.util.Serial
import java.util.*

class MutableSerial {
    private val serialMap: MutableMap<Serial, BaseSerial> = EnumMap(Serial::class.java)
    var listener: (Serial, String) -> Unit = { _, _ -> }

    @Synchronized
    fun init(serial: Serial, baudRate: Int, sendDelay: Long = 30L, readDelay: Long = 30L): Int {
        return init(serial, baudRate, 1, 8, 0, 0, sendDelay, readDelay)
    }

    @Synchronized
    fun init(
        serial: Serial,
        baudRate: Int,
        stopBits: Int,
        dataBits: Int,
        parity: Int,
        flowCon: Int,
        sendDelay: Long,
        readDelay: Long
    ): Int {
        require(baudRate != 0) { "Serial port and baud rate cannot be empty" }

        val currentSerial = serialMap[serial]
        if (currentSerial != null && currentSerial.isOpen) {
            return 1
        }

        val baseSerial = object : BaseSerial(serial, baudRate) {}.apply {
            this.onDataReceived = { data: String ->
                listener.invoke(serial, data)
            }
            this.stopBits = stopBits
            this.dataBits = dataBits
            this.parity = parity
            this.flowCon = flowCon
            this.readDelay = readDelay
            this.sendDelay = sendDelay
        }

        val openStatus = baseSerial.init()

        if (openStatus == 0) {
            serialMap[serial] = baseSerial
        } else {
            baseSerial.close()
        }

        return openStatus
    }

    /**
     * 串口是否已经打开
     * Serial port status (open/close)
     */
    fun isOpenSerial(serial: Serial): Boolean {
        val currentSerial = serialMap[serial]
        return currentSerial != null && currentSerial.isOpen
    }

    /**
     * Close the serial port
     */
    fun close(serial: Serial) {
        val currentSerial = serialMap[serial]
        if (currentSerial != null) {
            currentSerial.close()
            serialMap.remove(serial)
        } else {
            Log.e(TAG, "The serial port is closed or not initialized")
        }
    }

    /**
     * send data
     *
     * @param serial
     * @param hexData
     */
    fun sendHex(serial: Serial, hexData: String) {
        val currentSerial = serialMap[serial]
        if (currentSerial != null && currentSerial.isOpen) {
            val dateTrim = hexData.trim { it <= ' ' }.replace(" ", "")
            currentSerial.sendHex(dateTrim)
        } else {
            Log.e(TAG, "The serial port is closed or not initialized")
        }
    }

    /**
     *  send data
     *
     *  @param serial
     *  @param byteData
     */
    fun sendByte(serial: Serial, byteData: ByteArray) {
        val currentSerial = serialMap[serial]
        if (currentSerial != null && currentSerial.isOpen) {
            currentSerial.sendByteArray(byteData)
        } else {
            Log.e(TAG, "The serial port is closed or not initialized")
        }
    }

    /**
     *  send data
     *
     *  @param serial
     *  @param strData
     */
    fun sendText(serial: Serial, strData: String) {
        val currentSerial = serialMap[serial]
        if (currentSerial != null && currentSerial.isOpen) {
            currentSerial.sendText(strData)
        } else {
            Log.e(TAG, "The serial port is closed or not initialized")
        }
    }

    companion object {
        private const val TAG = "MutableSerial"

        @JvmStatic
        val instance: MutableSerial by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MutableSerial()
        }
    }
}