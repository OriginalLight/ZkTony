package com.zktony.android.data

import com.zktony.serialport.ext.writeInt16LE
import com.zktony.serialport.ext.writeInt8

data class PumpControl(
    val control: Int = 0,
    val direction: Int = 0,
    val speedUnit: Int = 0,
    val speed: String = "0",
    val time: String = "0"
) {
    fun toByteArray(): ByteArray {
        val byteArray = ByteArray(7)
        byteArray.writeInt8(speedUnit, 0)
        byteArray.writeInt8(control, 1)
        byteArray.writeInt8(direction, 2)
        byteArray.writeInt16LE(speed.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 3)
        byteArray.writeInt16LE(time.toBigDecimal().toInt(), 5)
        return byteArray
    }
}