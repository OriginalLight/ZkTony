package com.zktony.serialport

import android.util.Log
import com.zktony.serialport.util.Serial
import java.util.*

class MutableSerial {
    private val serialMap: MutableMap<Serial, BaseSerial> = EnumMap(Serial::class.java)
    var listener: (Serial, String) -> Unit = { _, _ -> }

    @Synchronized
    fun init(portStr: Serial, baudRate: Int): Int {
        return init(portStr, baudRate, 1, 8, 0, 0)
    }

    @Synchronized
    fun init(
        portStr: Serial,
        baudRate: Int,
        stopBits: Int,
        dataBits: Int,
        parity: Int,
        flowCon: Int
    ): Int {
        require(baudRate != 0) { "Serial port and baud rate cannot be empty" }
        val serial = serialMap[portStr]
        if (serial != null && serial.isOpen) {
            return 1
        }
        val baseSerial: BaseSerial = object : BaseSerial(portStr, baudRate) {}.apply {
            this.onDataReceived = { data: String ->
                listener.invoke(portStr, data)
            }
            this.stopBits = stopBits
            this.dataBits = dataBits
            this.parity = parity
            this.flowCon = flowCon
        }
        val openStatus = baseSerial.openSerial()
        if (openStatus == 0) {
            serialMap[portStr] = baseSerial
        } else {
            baseSerial.close()
        }
        return openStatus
    }

    /**
     * 串口是否已经打开
     * Serial port status (open/close)
     */
    fun isOpenSerial(portStr: Serial): Boolean {
        val serial = serialMap[portStr]
        return serial != null && serial.isOpen
    }

    /**
     * Close the serial port
     */
    fun close(portStr: Serial) {
        val serial = serialMap[portStr]
        if (serial != null) {
            serial.close()
            serialMap.remove(portStr)
        } else {
            Log.e(TAG, "The serial port is closed or not initialized")
        }
    }

    /**
     * send data
     *
     * @param portStr
     * @param hexData
     */
    fun sendHex(portStr: Serial, hexData: String) {
        val serial = serialMap[portStr]
        if (serial != null && serial.isOpen) {
            val dateTrim = hexData.trim { it <= ' ' }.replace(" ", "")
            serial.sendHex(dateTrim)
        } else {
            Log.e(TAG, "The serial port is closed or not initialized")
        }
    }

    /**
     *  send data
     *
     *  @param portStr
     *  @param byteData
     */
    fun sendByte(portStr: Serial, byteData: ByteArray) {
        val serial = serialMap[portStr]
        if (serial != null && serial.isOpen) {
            serial.sendByteArray(byteData)
        } else {
            Log.e(TAG, "The serial port is closed or not initialized")
        }
    }

    /**
     *  send data
     *
     *  @param portStr
     *  @param strData
     */
    fun sendText(portStr: Serial, strData: String) {
        val serial = serialMap[portStr]
        if (serial != null && serial.isOpen) {
            serial.sendText(strData)
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