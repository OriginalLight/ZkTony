package com.zktony.serialport.lifecycle

interface Callback {
    fun callback(byteArray: ByteArray)

    fun exception(ex: Exception)
}