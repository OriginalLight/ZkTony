package com.zktony.serialport.listener

interface OnComDataListener {
    fun comDataBack(com: String, hexData: String)
}