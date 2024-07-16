package com.zktony.android.data

import com.zktony.serialport.ext.writeInt16LE
import com.zktony.serialport.ext.writeInt8

data class VoltageControl(
    val mode: Int = 0,
    val voltage: String = "0",
    val current: String = "0",
    val power: String = "0"
) {
    fun toByteArray(): ByteArray {
        val byteArray = ByteArray(7)
        byteArray.writeInt8(mode + 1, 0)
        byteArray.writeInt16LE(voltage.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 1)
        byteArray.writeInt16LE(current.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 3)
        byteArray.writeInt16LE(power.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 5)
        return byteArray
    }
}