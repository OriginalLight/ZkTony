package com.zktony.android.data

import com.zktony.serialport.ext.writeInt16LE
import com.zktony.serialport.ext.writeInt8

data class VoltageControl(
    val mode : Int = 0,
    val voltage : Double = 0.0,
    val current : Double = 0.0,
    val power : Double = 0.0
) {
    fun toByteArray(): ByteArray {
        val byteArray = ByteArray(7)
        byteArray.writeInt8(mode + 1, 0)
        byteArray.writeInt16LE((voltage * 100).toInt(), 1)
        byteArray.writeInt16LE((current * 100).toInt(), 3)
        byteArray.writeInt16LE((power * 100).toInt(), 5)
        return byteArray
    }
}