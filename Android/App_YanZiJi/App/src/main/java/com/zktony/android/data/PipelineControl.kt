package com.zktony.android.data

import com.zktony.serialport.ext.writeInt16LE

data class PipelineControl(
    val speed: String = "0",
    val time: String = "0"
) {
    fun toByteArray(): ByteArray {
        val byteArray = ByteArray(4)
        byteArray.writeInt16LE(speed.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 0)
        byteArray.writeInt16LE(time.toBigDecimal().toInt(), 2)
        return byteArray
    }
}
