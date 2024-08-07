package com.zktony.android.data

import com.zktony.log.LogUtils
import com.zktony.room.entities.LogSnapshot
import com.zktony.serialport.ext.readInt16LE
import com.zktony.serialport.ext.readInt8
import com.zktony.serialport.ext.readUInt32LE
import com.zktony.serialport.ext.toHexString
import java.math.RoundingMode

data class ChannelState(
    // 运行状态
    val runState: Int = 0,
    // 实验类型
    val experimentType: Int = 0,
    // 运行模式
    val experimentalMode: Int = 0,
    // 故障信息
    val errorInfo: Long = 0L,
    // 当前电流
    val current: String = "0",
    // 当前电压
    val voltage: String = "0",
    // 当前功率
    val power: String = "0",
    // 当前温度
    val temperature: String = "0",
    // 当前计时（秒，向上计时）
    val time: Int = 0,
    // 当前运行步骤
    val step: Int = 0,
    // 到位光耦1状态
    val opt1: Int = 0,
    // 到位光耦2状态
    val opt2: Int = 0,
    // 气泡传感器1状态
    val bub1: Int = 0,
    // 气泡传感器2状态
    val bub2: Int = 0
) {
    fun toLogSnapshot(subId: Long): LogSnapshot {
        return LogSnapshot(
            subId = subId,
            runState = runState,
            experimentType = experimentType,
            experimentalMode = experimentalMode,
            errorInfo = errorInfo,
            current = current,
            voltage = voltage,
            power = power,
            temperature = temperature,
            time = time,
            step = step,
            opt1 = opt1,
            opt2 = opt2,
            bub1 = bub1,
            bub2 = bub2
        )
    }

    companion object {
        fun fromByteArray(byteArray: ByteArray): ChannelState? {
            try {
                if (byteArray.size != 21) throw Exception("ChannelState 长度不正确 ${byteArray.toHexString()}")
                val voltage = byteArray.readInt16LE(7).toBigDecimal().divide(100.toBigDecimal())
                    .setScale(2, RoundingMode.HALF_UP)
                val current = byteArray.readInt16LE(9).toBigDecimal().divide(100.toBigDecimal())
                    .setScale(2, RoundingMode.HALF_UP)
                return ChannelState(
                    runState = byteArray.readInt8(0),
                    experimentType = byteArray.readInt8(1),
                    experimentalMode = byteArray.readInt8(2),
                    errorInfo = byteArray.readUInt32LE(3),
                    voltage = voltage.toPlainString(),
                    current = current.toPlainString(),
                    power = voltage.multiply(current).setScale(2, RoundingMode.HALF_UP)
                        .toPlainString(),
                    temperature = byteArray.readInt16LE(11).toBigDecimal()
                        .divide(100.toBigDecimal()).setScale(2, RoundingMode.HALF_UP)
                        .toPlainString(),
                    time = byteArray.readInt16LE(13),
                    step = byteArray.readInt16LE(15),
                    opt1 = byteArray.readInt8(17),
                    opt2 = byteArray.readInt8(18),
                    bub1 = byteArray.readInt8(19),
                    bub2 = byteArray.readInt8(20)
                )
            } catch (e: Exception) {
                LogUtils.error("ChannelState 解析失败 ${e.printStackTrace()}", true)
                return null
            }
        }
    }
}