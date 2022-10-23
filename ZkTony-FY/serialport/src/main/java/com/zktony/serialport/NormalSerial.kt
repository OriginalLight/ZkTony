package com.zktony.serialport

import android.text.TextUtils
import com.zktony.serialport.listener.OnNormalDataListener
import com.zktony.serialport.listener.OnSerialDataListener
import com.zktony.serialport.util.Logger

class NormalSerial {
    private var mBaseSerial: BaseSerialPort? = null
    private var mListener: MutableList<OnNormalDataListener>? = null

    @Synchronized
    fun open(portStr: String, ibaudRate: Int): Int {
        return open(portStr, ibaudRate, 1, 8, 0, 0)
    }

    @Synchronized
    fun open(
        portStr: String,
        ibaudRate: Int,
        mStopBits: Int,
        mDataBits: Int,
        mParity: Int,
        mFlowCon: Int
    ): Int {
        require(!(TextUtils.isEmpty(portStr) || ibaudRate == 0)) { "Serial port and baud rate cannot be empty" }
        if (mBaseSerial != null) {
            close()
        }
        mBaseSerial = object : BaseSerialPort(portStr, ibaudRate) {
            override fun onDataBack(data: String) {
                //温度
                if (mListener != null) {
                    for (i in mListener!!.indices.reversed()) {
                        mListener!![i].normalDataBack(data)
                    }
                }
            }
        }
        (mBaseSerial as BaseSerialPort).stopBits = mStopBits
        (mBaseSerial as BaseSerialPort).dataBits = mDataBits
        (mBaseSerial as BaseSerialPort).parity = mParity
        (mBaseSerial as BaseSerialPort).flowCon = mFlowCon
        val openStatus = (mBaseSerial as BaseSerialPort).openSerial()
        if (openStatus != 0) {
            close()
        }
        return openStatus
    }

    /**
     * 添加串口返回数据回调
     * Add callback
     */
    fun addDataListener(dataListener: OnNormalDataListener) {
        if (mListener == null) {
            mListener = ArrayList()
        }
        mListener!!.add(dataListener)
    }

    /**
     * 移除串口返回数据回调
     * Remove callback
     */
    fun removeDataListener(dataListener: OnNormalDataListener) {
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
    fun setSerialDataListener(dataListener: OnSerialDataListener?) {
        if (mBaseSerial != null) {
            mBaseSerial!!.setSerialDataListener(dataListener)
        } else {
            Logger.instance.e(TAG, "The serial port is closed or not initialized")
            //throw new IllegalArgumentException("The serial port is closed or not initialized");
        }
    }//throw new IllegalArgumentException("The serial port is closed or not initialized");

    /**
     * 串口是否打开
     * Serial port status (open/close)
     *
     * @return true/false
     */
    val isOpen: Boolean
        get() = if (mBaseSerial != null) {
            mBaseSerial!!.isOpen
        } else {
            Logger.instance.e(TAG, "The serial port is closed or not initialized")
            //throw new IllegalArgumentException("The serial port is closed or not initialized");
            false
        }

    /**
     * Close the serial port
     */
    fun close() {
        if (mBaseSerial != null) {
            mBaseSerial!!.close()
            mBaseSerial = null
        } else {
            Logger.instance.e(TAG, "The serial port is closed or not initialized")
            //throw new IllegalArgumentException("The serial port is closed or not initialized");
        }
    }

    /**
     * send data
     *
     * @param hexData
     */
    fun sendHex(hexData: String?) {
        if (isOpen) {
            mBaseSerial!!.sendHex(hexData!!)
        }
    }

    companion object {
        private const val TAG = "NormalSerial"

        @JvmStatic
        val instance: NormalSerial by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            NormalSerial()
        }
    }
}