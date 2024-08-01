package com.zktony.android.data

import com.zktony.log.LogUtils
import com.zktony.serialport.ext.readInt16LE
import com.zktony.serialport.ext.readInt32LE
import com.zktony.serialport.ext.readInt8
import com.zktony.serialport.ext.toHexString
import java.math.RoundingMode

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
    val current: String = "0",
    // 当前电压
    val voltage: String = "0",
    // 当前功率
    val power: String = "0",
    // 当前温度
    val temperature: String = "0",
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
        if (byteArray.size != 21) throw Exception("ChannelState 长度不正确 ${byteArray.toHexString()}")
        val voltage = byteArray.readInt16LE(7).toBigDecimal().divide(100.toBigDecimal())
        val current = byteArray.readInt16LE(9).toBigDecimal().divide(100.toBigDecimal())
        return ChannelState(
            runState = byteArray.readInt8(0),
            experimentType = byteArray.readInt8(1),
            runMode = byteArray.readInt8(2),
            faultInfo = byteArray.readInt32LE(3),
            voltage = voltage.stripTrailingZeros().toPlainString(),
            current = current.stripTrailingZeros().toPlainString(),
            power = voltage.multiply(current).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros()
                .toPlainString(),
            temperature = byteArray.readInt16LE(11).toBigDecimal().divide(100.toBigDecimal())
                .stripTrailingZeros().toPlainString(),
            timing = byteArray.readInt16LE(13),
            step = byteArray.readInt16LE(15),
            opto1 = byteArray.readInt8(17),
            opto2 = byteArray.readInt8(18),
            bubble1 = byteArray.readInt8(19),
            bubble2 = byteArray.readInt8(20)
        )
    } catch (e: Exception) {
        LogUtils.error("ChannelState 解析失败 ${e.printStackTrace()}", true)
        return null
    }
}