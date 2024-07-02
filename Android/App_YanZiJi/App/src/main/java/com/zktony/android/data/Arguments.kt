package com.zktony.android.data

import com.zktony.serialport.ext.readInt16BE
import com.zktony.serialport.ext.readInt16LE

data class Arguments(
    // 实验参数
    val outFillSpeed: Double = 0.0,       // 出液蠕动泵填充速度(2byte)
    val inFillSpeed: Double = 0.0,        // 进液蠕动泵填充速度(2byte)
    val outDrainSpeed: Double = 0.0,      // 出液蠕动泵排液速度(2byte)
    val inDrainSpeed: Double = 0.0,       // 进液蠕动泵排液速度(2byte)
    val outFillTime: Int = 0,        // 出液蠕动泵填充时间(2byte)
    val inFillTime: Int = 0,         // 进液蠕动泵填充时间(2byte)
    val outDrainTime: Int = 0,       // 出液蠕动泵排液时间(2byte)
    val inDrainTime: Int = 0,        // 进液蠕动泵排液时间(2byte)
    val inEmptyTime: Int = 0,        // 进液蠕动泵排空时间(2byte)
    val inOutScale: Double = 0.0,    // 进出液速度比值(2byte)

    // 清洗参数
    val cleanOutFillSpeed: Double = 0.0,       // 出液蠕动泵填充速度(2byte)
    val cleanInFillSpeed: Double = 0.0,        // 进液蠕动泵填充速度(2byte)
    val cleanOutDrainSpeed: Double = 0.0,      // 出液蠕动泵排液速度(2byte)
    val cleanInDrainSpeed: Double = 0.0,       // 进液蠕动泵排液速度(2byte)
    val cleanOutFillTime: Int = 0,        // 出液蠕动泵填充时间(2byte)
    val cleanInFillTime: Int = 0,         // 进液蠕动泵填充时间(2byte)
    val cleanOutDrainTime: Int = 0,       // 出液蠕动泵排液时间(2byte)
    val cleanInDrainTime: Int = 0,        // 进液蠕动泵排液时间(2byte)
    val cleanInEmptyTime: Int = 0,        // 进液蠕动泵排空时间(2byte)
    val cleanOutScale: Double = 0.0,      // 进出液速度比值(2byte)

    // 补偿参数
    val tempComp: List<Double> = List(10) { 0.0 },            // 温度补偿值(2byte)*10
    val outSpeedComp: List<Double> = List(10) { 0.0 },        // 出液转速补偿值(2byte)*10
    val inSpeedComp: List<Double> = List(10) { 0.0 },         // 进液转速补偿值(2byte)*10
    val voltComp: List<Double> = List(10) { 0.0 },            // 电压补偿值(2byte)*10
    val currComp: List<Double> = List(10) { 0.0 },            // 电流补偿值(2byte)*10

    // 光耦阈值
    val outBubbleThreshold: Double = 0.0,    // 出液气泡光耦阈值(2byte)
    val inBubbleThreshold: Double = 0.0      // 进液气泡光耦阈值(2byte)
) {
    // 实验参数
    fun toExperimental(): ArgumentsExperimental {
        return ArgumentsExperimental(
            outFillSpeed = outFillSpeed,
            inFillSpeed = inFillSpeed,
            outDrainSpeed = outDrainSpeed,
            inDrainSpeed = inDrainSpeed,
            outFillTime = outFillTime,
            inFillTime = inFillTime,
            outDrainTime = outDrainTime,
            inDrainTime = inDrainTime,
            inEmptyTime = inEmptyTime,
            inOutScale = inOutScale
        )
    }
    // 清洗参数
    fun toClean(): ArgumentsClean {
        return ArgumentsClean(
            cleanOutFillSpeed = cleanOutFillSpeed,
            cleanInFillSpeed = cleanInFillSpeed,
            cleanOutDrainSpeed = cleanOutDrainSpeed,
            cleanInDrainSpeed = cleanInDrainSpeed,
            cleanOutFillTime = cleanOutFillTime,
            cleanInFillTime = cleanInFillTime,
            cleanOutDrainTime = cleanOutDrainTime,
            cleanInDrainTime = cleanInDrainTime,
            cleanInEmptyTime = cleanInEmptyTime,
            cleanOutScale = cleanOutScale
        )
    }
    // 温度补偿参数
    fun toTemperature(): ArgumentsTemperature {
        return ArgumentsTemperature(
            tempComp = tempComp
        )
    }
    // 速度补偿参数
    fun toSpeed(): ArgumentsSpeed {
        return ArgumentsSpeed(
            outSpeedComp = outSpeedComp,
            inSpeedComp = inSpeedComp
        )
    }
    // 电压补偿参数
    fun toVoltage(): ArgumentsVoltage {
        return ArgumentsVoltage(
            voltComp = voltComp
        )
    }
    // 电流补偿参数
    fun toCurrent(): ArgumentsCurrent {
        return ArgumentsCurrent(
            currComp = currComp
        )
    }
    // 光耦阈值
    fun toBubble(): ArgumentsBubble {
        return ArgumentsBubble(
            outBubbleThreshold = outBubbleThreshold,
            inBubbleThreshold = inBubbleThreshold
        )
    }
    // bytes to Arguments
    fun toArguments(bytes: ByteArray): Arguments? {
        if (bytes.size != 144) return null
        return Arguments(
            outFillSpeed = bytes.readInt16LE(0) / 100.0,
            inFillSpeed = bytes.readInt16LE(2) / 100.0,
            outDrainSpeed = bytes.readInt16LE(4) / 100.0,
            inDrainSpeed = bytes.readInt16LE(6) / 100.0,
            outFillTime = bytes.readInt16LE(8),
            inFillTime = bytes.readInt16LE(10),
            outDrainTime = bytes.readInt16LE(12),
            inDrainTime = bytes.readInt16LE(14),
            inEmptyTime = bytes.readInt16LE(16),
            inOutScale = bytes.readInt16LE(18) / 100.0,
            cleanOutFillSpeed = bytes.readInt16LE(20) / 100.0,
            cleanInFillSpeed = bytes.readInt16LE(22) / 100.0,
            cleanOutDrainSpeed = bytes.readInt16LE(24) / 100.0,
            cleanInDrainSpeed = bytes.readInt16LE(26) / 100.0,
            cleanOutFillTime = bytes.readInt16LE(28),
            cleanInFillTime =   bytes.readInt16LE(30),
            cleanOutDrainTime = bytes.readInt16LE(32),
            cleanInDrainTime = bytes.readInt16LE(34),
            cleanInEmptyTime = bytes.readInt16LE(36),
            cleanOutScale = bytes.readInt16LE(38) / 100.0,
            tempComp = toDoubleList(bytes.copyOfRange(40, 60)),
            outSpeedComp = toDoubleList(bytes.copyOfRange(60, 80)),
            inSpeedComp = toDoubleList(bytes.copyOfRange(80, 100)),
            voltComp = toDoubleList(bytes.copyOfRange(100, 120)),
            currComp = toDoubleList(bytes.copyOfRange(120, 140)),
            outBubbleThreshold = bytes.readInt16LE(140) / 100.0,
            inBubbleThreshold = bytes.readInt16LE(142) / 100.0
        )
    }

    private fun toDoubleList(bytes: ByteArray): List<Double> {
        val list = mutableListOf<Double>()
        for (i in bytes.indices step 2) {
            val value = bytes.readInt16LE(i) / 100.0
            list.add(value)
        }
        return list
    }
}

data class ArgumentsExperimental(
    // 实验参数
    val outFillSpeed: Double = 0.0,       // 出液蠕动泵填充速度(2byte)
    val inFillSpeed: Double = 0.0,        // 进液蠕动泵填充速度(2byte)
    val outDrainSpeed: Double = 0.0,      // 出液蠕动泵排液速度(2byte)
    val inDrainSpeed: Double = 0.0,       // 进液蠕动泵排液速度(2byte)
    val outFillTime: Int = 0,        // 出液蠕动泵填充时间(2byte)
    val inFillTime: Int = 0,         // 进液蠕动泵填充时间(2byte)
    val outDrainTime: Int = 0,       // 出液蠕动泵排液时间(2byte)
    val inDrainTime: Int = 0,        // 进液蠕动泵排液时间(2byte)
    val inEmptyTime: Int = 0,        // 进液蠕动泵排空时间(2byte)
    val inOutScale: Double = 0.0     // 进出液速度比值(2byte)
) {
    fun toByteArray(): ByteArray {
        val bytes = ByteArray(20)
        return bytes
    }
}

data class ArgumentsClean(
    // 清洗参数
    val cleanOutFillSpeed: Double = 0.0,       // 出液蠕动泵填充速度(2byte)
    val cleanInFillSpeed: Double = 0.0,        // 进液蠕动泵填充速度(2byte)
    val cleanOutDrainSpeed: Double = 0.0,      // 出液蠕动泵排液速度(2byte)
    val cleanInDrainSpeed: Double = 0.0,       // 进液蠕动泵排液速度(2byte)
    val cleanOutFillTime: Int = 0,        // 出液蠕动泵填充时间(2byte)
    val cleanInFillTime: Int = 0,         // 进液蠕动泵填充时间(2byte)
    val cleanOutDrainTime: Int = 0,       // 出液蠕动泵排液时间(2byte)
    val cleanInDrainTime: Int = 0,        // 进液蠕动泵排液时间(2byte)
    val cleanInEmptyTime: Int = 0,        // 进液蠕动泵排空时间(2byte)
    val cleanOutScale: Double = 0.0       // 进出液速度比值(2byte)
)

data class ArgumentsTemperature(
    // 温度补偿参数
    val tempComp: List<Double> = List(10) { 0.0 }            // 温度补偿值(2byte)*10
)

data class ArgumentsSpeed(
    // 速度补偿参数
    val outSpeedComp: List<Double> = List(10) { 0.0 },        // 出液转速补偿值(2byte)*10
    val inSpeedComp: List<Double> = List(10) { 0.0 }          // 进液转速补偿值(2byte)*10
)

data class ArgumentsVoltage(
    // 电压补偿参数
    val voltComp: List<Double> = List(10) { 0.0 }            // 电压补偿值(2byte)*10
)

data class ArgumentsCurrent(
    // 电流补偿参数
    val currComp: List<Double> = List(10) { 0.0 }            // 电流补偿值(2byte)*10
)

data class ArgumentsBubble(
    // 光耦阈值
    val outBubbleThreshold: Double = 0.0,    // 出液气泡光耦阈值(2byte)
    val inBubbleThreshold: Double = 0.0      // 进液气泡光耦阈值(2byte)
)
