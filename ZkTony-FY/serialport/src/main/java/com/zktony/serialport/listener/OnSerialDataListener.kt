package com.zktony.serialport.listener

/**
 * Serial communication data monitoring
 */
interface OnSerialDataListener {
    /**
     * Data sent by serial port
     */
    fun onSend(hexData: String)

    /**
     * Data received by serial port
     */
    fun onReceive(hexData: String)

    /**
     * Data received by serial port (return complete hex string)
     */
    fun onReceiveFullData(hexData: String)
}