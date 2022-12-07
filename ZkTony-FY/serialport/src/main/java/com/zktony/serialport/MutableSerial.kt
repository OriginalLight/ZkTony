package com.zktony.serialport

import com.zktony.serialport.util.Logger

class MutableSerial {
    private val serialPortMap: MutableMap<String, SerialPortHelper> = HashMap()
    var listener: (String, String) -> Unit = { _, _ -> }

    @Synchronized
    fun init(portStr: String, baudRate: Int): Int {
        return init(portStr, baudRate, 1, 8, 0, 0)
    }

    @Synchronized
    fun init(
        portStr: String, baudRate: Int, stopBits: Int, dataBits: Int, parity: Int, flowCon: Int
    ): Int {
        require(!(portStr.isEmpty() || baudRate == 0)) { "Serial port and baud rate cannot be empty" }
        val serial = serialPortMap[portStr]
        if (serial != null && serial.isOpen) {
            return 1
        }
        val serialPortHelper: SerialPortHelper =
            object : SerialPortHelper(portStr, baudRate) {}.apply {
                    this.onDataReceived = { data: String ->
                        listener.invoke(portStr, data)
                    }
                    this.stopBits = stopBits
                    this.dataBits = dataBits
                    this.parity = parity
                    this.flowCon = flowCon
                }
        val openStatus = serialPortHelper.openSerial()
        if (openStatus == 0) {
            serialPortMap[portStr] = serialPortHelper
        } else {
            serialPortHelper.close()
        }
        return openStatus
    }

    /**
     * 串口是否已经打开
     * Serial port status (open/close)
     */
    fun isOpenSerial(portStr: String): Boolean {
        val serial = serialPortMap[portStr]
        return serial != null && serial.isOpen
    }

    /**
     * Close the serial port
     */
    fun close(portStr: String) {
        val baseSerial = serialPortMap[portStr]
        if (baseSerial != null) {
            baseSerial.close()
        } else {
            Logger.instance.e(TAG, "The serial port is closed or not initialized")
        }
    }

    /**
     * send data
     *
     * @param portStr
     * @param hexData
     */
    fun sendHex(portStr: String, hexData: String) {
        if (portStr.isEmpty()) {
            Logger.instance.e(TAG, "The serial port is empty")
            return
        }
        val baseSerial = serialPortMap[portStr]
        if (baseSerial != null && baseSerial.isOpen) {
            val dateTrim = hexData.trim { it <= ' ' }.replace(" ", "")
            baseSerial.sendHex(dateTrim)
        } else {
            Logger.instance.e(TAG, "The serial port is closed or not initialized")
        }
    }

    /**
     *  send data
     *
     *  @param portStr
     *  @param byteData
     */
    fun sendByte(portStr: String, byteData: ByteArray) {
        if (portStr.isEmpty()) {
            Logger.instance.e(TAG, "The serial port is empty")
            return
        }
        val baseSerial = serialPortMap[portStr]
        if (baseSerial != null && baseSerial.isOpen) {
            baseSerial.sendByteArray(byteData)
        } else {
            Logger.instance.e(TAG, "The serial port is closed or not initialized")
        }
    }

    /**
     *  send data
     *
     *  @param portStr
     *  @param strData
     */
    fun sendText(portStr: String, strData: String) {
        if (portStr.isEmpty()) {
            Logger.instance.e(TAG, "The serial port is empty")
            return
        }
        val baseSerial = serialPortMap[portStr]
        if (baseSerial != null && baseSerial.isOpen) {
            baseSerial.sendText(strData)
        } else {
            Logger.instance.e(TAG, "The serial port is closed or not initialized")
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