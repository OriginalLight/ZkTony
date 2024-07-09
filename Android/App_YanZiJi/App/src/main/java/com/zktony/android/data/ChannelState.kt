package com.zktony.android.data

import com.zktony.log.LogUtils
import com.zktony.serialport.ext.readFloatLE
import com.zktony.serialport.ext.readInt16LE
import com.zktony.serialport.ext.readInt32LE
import com.zktony.serialport.ext.readInt8

data class ChannelState(
    // 运行状态
    val runState: Int = 0,
    // 实验类型
    val experimentType: Int = 0,
    // 运行模式
    val runMode: Int = 0,
    // 故障信息
    val faultInfo: Int = 0,
    // 当前电流
    val current: Double = 0.0,
    // 当前电压
    val voltage: Double = 0.0,
    // 当前功率
    val power: Double = 0.0,
    // 当前温度
    val temperature: Double = 0.0,
    // 当前计时（秒，向上计时）
    val timing: Int = 0,
    // 当前运行步骤
    val step: Int = 0,
    // 到位光耦1状态
    val opto1: Int = 0,
    // 到位光耦2状态
    val opto2: Int = 0,
    // 气泡传感器1状态
    val bubble1: Int = 0,
    // 气泡传感器2状态
    val bubble2: Int = 0,
)

fun toChannelState(byteArray: ByteArray): ChannelState? {
    try {
        if (byteArray.size != 21) throw Exception("ChannelState 长度不正确")
        return ChannelState(
            runState = byteArray.readInt8(0),
            experimentType = byteArray.readInt8(1),
            runMode = byteArray.readInt8(2),
            faultInfo = byteArray.readInt32LE(3),
            current = byteArray.readInt16LE(7) / 100.0,
            voltage = byteArray.readInt16LE(9) / 100.0,
            power = (byteArray.readInt16LE(7) / 100.0) * (byteArray.readFloatLE(9) / 100.0),
            temperature = byteArray.readInt16LE(11) / 100.0,
            timing = byteArray.readInt16LE(13),
            step = byteArray.readInt16LE(15),
            opto1 = byteArray.readInt8(17),
            opto2 = byteArray.readInt8(18),
            bubble1 = byteArray.readInt8(19),
            bubble2 = byteArray.readInt8(20),
        )
    } catch (e: Exception) {
        LogUtils.error("ChannelState 解析失败 ${e.printStackTrace()}", true)
        return null
    }
}