package com.zktony.serialport

import android.text.TextUtils
import com.zktony.serialport.listener.OnComDataListener
import com.zktony.serialport.listener.OnSerialDataListener
import com.zktony.serialport.util.Logger

class COMSerial {
    private val mBaseSerials: MutableMap<String, BaseSerialPort> = HashMap()
    private var mListener: MutableList<OnComDataListener>? = null

    @Synchronized
    fun addCOM(portStr: String, ibaudRate: Int): Int {
        return addCOM(portStr, ibaudRate, 1, 8, 0, 0)
    }

    @Synchronized
    fun addCOM(
        portStr: String,
        ibaudRate: Int,
        mStopBits: Int,
        mDataBits: Int,
        mParity: Int,
        mFlowCon: Int
    ): Int {
        require(!(TextUtils.isEmpty(portStr) || ibaudRate == 0)) { "Serial port and baud rate cannot be empty" }
        val baseSerials = mBaseSerials[portStr]
        if (baseSerials != null && baseSerials.isOpen) {
            return 1
        }
        val mBaseSerial: BaseSerialPort = object : BaseSerialPort(portStr, ibaudRate) {
            override fun onDataBack(data: String) {
                //温度
                if (mListener != null) {
                    for (i in mListener!!.indices.reversed()) {
                        mListener!![i].comDataBack(portStr, data)
                    }
                }
            }
        }
        mBaseSerial.stopBits = mStopBits
        mBaseSerial.dataBits = mDataBits
        mBaseSerial.parity = mParity
        mBaseSerial.flowCon = mFlowCon
        val openStatus = mBaseSerial.openSerial()
        if (openStatus != 0) {
            mBaseSerial.close()
        } else {
            mBaseSerials[portStr] = mBaseSerial
        }
        return openStatus
    }

    /**
     * 串口是否已经打开
     * Serial port status (open/close)
     */
    fun isOpenSerial(portStr: String): Boolean {
        val baseSerials = mBaseSerials[portStr]
        return baseSerials != null && baseSerials.isOpen
    }

    /**
     * 添加串口返回数据回调
     * Add callback
     */
    fun addDataListener(dataListener: OnComDataListener) {
        if (mListener == null) {
            mListener = ArrayList()
        }
        mListener!!.add(dataListener)
    }

    /**
     * 移除串口返回数据回调
     * Remove callback
     */
    fun removeDataListener(dataListener: OnComDataListener) {
        if (mListener != null) {
            mListener!!.remove(dataListener)
        }
    }

    /**
     * 移除全部回调
     * Remove all
     */
    fun clearAllDataListener() {
        if (mListener != null) {
            mListener!!.clear()
        }
    }

    /**
     * 监听串口数据
     * Listening to serial data
     * 该方法必须在串口打开成功后调用
     * This method must be called after the serial port is successfully opened.
     */
    fun setSerialDataListener(portStr: String, dataListener: OnSerialDataListener?) {
        val baseSerial = mBaseSerials[portStr]
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
        val baseSerial = mBaseSerials[portStr]
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
        val baseSerial = mBaseSerials[portStr]
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
        val baseSerial = mBaseSerials[portStr]
        if (baseSerial != null && baseSerial.isOpen) {
            val dateTrim = hexData.trim { it <= ' ' }.replace(" ", "")
            baseSerial.sendHex(dateTrim)
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