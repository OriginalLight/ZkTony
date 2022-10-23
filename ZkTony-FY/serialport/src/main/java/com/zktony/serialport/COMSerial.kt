package com.zktony.serialport

import android.text.TextUtils
import com.zktony.serialport.listener.OnComDataListener
import com.zktony.serialport.listener.OnSerialDataListener
import com.zktony.serialport.util.Logger

class COMSerial {
    private val baseSerials: MutableMap<String, BaseSerialPort> = HashMap()
    private var listener: MutableList<OnComDataListener>? = null

    @Synchronized
    fun addCOM(portStr: String, baudRate: Int): Int {
        return addCOM(portStr, baudRate, 1, 8, 0, 0)
    }

    @Synchronized
    fun addCOM(
        portStr: String,
        baudRate: Int,
        stopBits: Int,
        dataBits: Int,
        parity: Int,
        flowCon: Int
    ): Int {
        require(!(TextUtils.isEmpty(portStr) || baudRate == 0)) { "Serial port and baud rate cannot be empty" }
        val baseSerials = baseSerials[portStr]
        if (baseSerials != null && baseSerials.isOpen) {
            return 1
        }
        val baseSerial: BaseSerialPort = object : BaseSerialPort(portStr, baudRate) {
            override fun onDataBack(data: String) {
                //温度
                if (listener != null) {
                    for (i in listener!!.indices.reversed()) {
                        listener!![i].comDataBack(portStr, data)
                    }
                }
            }
        }
        baseSerial.stopBits = stopBits
        baseSerial.dataBits = dataBits
        baseSerial.parity = parity
        baseSerial.flowCon = flowCon
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
    fun addDataListener(dataListener: OnComDataListener) {
        if (listener == null) {
            listener = ArrayList()
        }
        listener!!.add(dataListener)
    }

    /**
     * 移除串口返回数据回调
     * Remove callback
     */
    fun removeDataListener(dataListener: OnComDataListener) {
        if (listener != null) {
            listener!!.remove(dataListener)
        }
    }

    /**
     * 移除全部回调
     * Remove all
     */
    fun clearAllDataListener() {
        if (listener != null) {
            listener!!.clear()
        }
    }

    /**
     * 监听串口数据
     * Listening to serial data
     * 该方法必须在串口打开成功后调用
     * This method must be called after the serial port is successfully opened.
     */
    fun setSerialDataListener(portStr: String, dataListener: OnSerialDataListener?) {
        val baseSerial = baseSerials[portStr]
        if (baseSerial != null) {
            baseSerial.setSerialDataListener(dataListener)
        } else {
            Logger.instance.e(TAG, "The serial port is closed or not initialized")
            //throw new IllegalArgumentException("The serial port is closed or not initialized");
        }
    }

    /**
     * 串口是否打开
     * Serial port status (open/close)
     *
     * @return true/false
     */
    fun isOpen(portStr: String): Boolean {
        val baseSerial = baseSerials[portStr]
        return if (baseSerial != null) {
            baseSerial.isOpen
        } else {
            Logger.instance.e(TAG, "The serial port is closed or not initialized")
            //throw new IllegalArgumentException("The serial port is closed or not initialized");
            false
        }
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
            //throw new IllegalArgumentException("The serial port is closed or not initialized");
        }
    }

    /**
     * send data
     *
     * @param portStr
     * @param hexData
     */
    fun sendHex(portStr: String, hexData: String) {
        if (TextUtils.isEmpty(portStr)) {
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
        if (TextUtils.isEmpty(portStr)) {
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
        if (TextUtils.isEmpty(portStr)) {
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
        private const val TAG = "COMSerial"

        @JvmStatic
        val instance: COMSerial by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            COMSerial()
        }
    }
}