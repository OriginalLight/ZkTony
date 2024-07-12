package com.zktony.android.data

import com.zktony.serialport.ext.readInt16LE
import com.zktony.serialport.ext.readInt8
import com.zktony.serialport.ext.writeInt16LE
import com.zktony.serialport.ext.writeInt8

data class Experimental(
    // 类型
    val type: Int,
    // 模式
    val mode: Int,
    // 速度
    val speed: Double,
    // 时间
    val time: Int,
    // 电压
    val voltage: Double,
    // 电流
    val current: Double,
    // 功率
    val power: Double,
    // 温度
    val temperature: Double
) {
    fun toByteArray(): ByteArray {
        val byteArray = ByteArray(14)
        byteArray.writeInt8(type, 0)
        byteArray.writeInt8(mode, 1)
        byteArray.writeInt16LE((speed * 100).toInt(), 2)
        byteArray.writeInt16LE(time * 60, 4)
        byteArray.writeInt16LE((voltage * 10).toInt(), 6)
        byteArray.writeInt16LE((current * 10).toInt(), 8)
        byteArray.writeInt16LE((power * 10).toInt(), 10)
        byteArray.writeInt16LE((temperature * 10).toInt(), 12)
        return byteArray
    }
}

fun toExperimental(byteArray: ByteArray): Experimental? {
    if (byteArray.size != 14) return null
    return try {
        Experimental(
            type = byteArray.readInt8(0),
            mode = byteArray.readInt8(1),
            speed = byteArray.readInt16LE(2) / 100.0,
            time = byteArray.readInt16LE(4) / 60,
            voltage = byteArray.readInt16LE(6) / 10.0,
            current = byteArray.readInt16LE(8) / 10.0,
            power = byteArray.readInt16LE(10) / 10.0,
            temperature = byteArray.readInt16LE(12) / 10.0
        )
    } catch (e: Exception) {
        null
    }
}