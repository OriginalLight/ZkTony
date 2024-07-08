package com.zktony.android.data

import com.zktony.serialport.ext.writeInt16LE
import com.zktony.serialport.ext.writeInt8

data class PumpControl(
    val control: Int,
    val direction: Int,
    val speedUnit: Int,
    val speed: Double,
    val time: Int
) {
    fun toByteArray(): ByteArray {
        val byteArray = ByteArray(7)
        byteArray.writeInt8(speedUnit, 0)
        byteArray.writeInt8(control, 1)
        byteArray.writeInt8(direction, 2)
        byteArray.writeInt16LE((speed * 100).toInt(), 3)
        byteArray.writeInt16LE(time * 60, 5)
        return byteArray
    }
}