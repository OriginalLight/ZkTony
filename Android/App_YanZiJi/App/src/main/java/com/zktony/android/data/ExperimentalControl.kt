package com.zktony.android.data

import com.zktony.room.entities.Program
import com.zktony.serialport.ext.writeInt16LE
import com.zktony.serialport.ext.writeInt8

data class ExperimentalControl(
    // 类型
    val experimentalType: Int = 0,
    // 模式
    val experimentalMode: Int = 0,
    // 速度
    val flowSpeed: String = "0",
    // 时间
    val time: String = "0",
    // 电压
    val voltage: String = "0",
    // 电流
    val current: String = "0",
    // 功率
    val power: String = "0",
    // 温度
    val temperature: String = "0"
) {
    fun toByteArray(): ByteArray {
        val byteArray = ByteArray(14)
        byteArray.writeInt8(experimentalType + 1, 0)
        byteArray.writeInt8(experimentalMode + 1, 1)
        byteArray.writeInt16LE(flowSpeed.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 2)
        byteArray.writeInt16LE(voltage.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 4)
        byteArray.writeInt16LE(current.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 6)
        byteArray.writeInt16LE(power.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 8)
        byteArray.writeInt16LE(time.toBigDecimal().toInt(), 10)
        byteArray.writeInt16LE(temperature.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 12)
        return byteArray
    }

    companion object {
        fun fromProgram(program: Program): ExperimentalControl {
            return ExperimentalControl(
                experimentalType = program.experimentalType,
                experimentalMode = program.experimentalMode,
                flowSpeed = program.flowSpeed,
                time = program.time.toBigDecimal().multiply(60.toBigDecimal()).toPlainString(),
                voltage = program.value,
                current = program.value,
                power = program.value,
                temperature = program.value
            )
        }
    }

}