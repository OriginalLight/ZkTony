package com.zktony.serialport

import com.zktony.serialport.util.Logger

class MutableSerial {
    private val baseSerials: MutableMap<String, BaseSerialPort> = HashMap()
    private var listener: (String, String) -> Unit = { _: String, _: String -> }

    @Synchronized
    fun init(portStr: String, baudRate: Int): Int {
        return init(portStr, baudRate, 1, 8, 0, 0)
    }

    @Synchronized
    fun init(
        portStr: String,
        baudRate: Int,
        stopBits: Int,
        dataBits: Int,
        parity: Int,
        flowCon: Int
    ): Int {
        require(!(portStr.isEmpty() || baudRate == 0)) { "Serial port and baud rate cannot be empty" }
        val baseSerials = baseSerials[portStr]
        if (baseSerials != null && baseSerials.isOpen) {
            return 1
        }
        val baseSerial: BaseSerialPort = object : BaseSerialPort(portStr, baudRate) {
            override fun onDataBack(data: String) {
                listener.invoke(portStr, data)
            }
        }.apply {
            this.stopBits = stopBits
            this.dataBits = dataBits
            this.parity = parity
            this.flowCon = flowCon
        }
        val openStatus = baseSerial.openSerial()
        if (openStatus != 0) {
            baseSerial.close()
        } else {
            this.baseSerials[portStr] = baseSerial
        }
        return openStatus
    }

    /**
     * 串口是否已经打开
     * Serial port status (open/close)
     */
    fun isOpenSerial(portStr: String): Boolean {
        val baseSerials = baseSerials[portStr]
        return baseSerials != null && baseSerials.isOpen
    }

    /**
     * 添加串口返回数据回调
     * Add callback
     */
    fun addListener(listener: (String, String) -> Unit) {
        this.listener = listener
    }

    /**
     * 移除串口返回数据回调
     * Remove callback
     */
    fun removeListener() {
        listener = { _: String, _: String -> }
    }

    /**
     * Close the serial port
     */
    fun close(portStr: String) {
        val baseSerial = baseSerials[portStr]
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
        val baseSerial = baseSerials[portStr]
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
        val baseSerial = baseSerials[portStr]
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
        val baseSerial = baseSerials[portStr]
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