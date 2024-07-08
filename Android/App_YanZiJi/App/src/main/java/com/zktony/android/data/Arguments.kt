package com.zktony.android.data

import com.zktony.serialport.ext.readInt16LE
import com.zktony.serialport.ext.writeInt16LE

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
    val emptyTime: Int = 0,        // 进液蠕动泵排空时间(2byte)
    val scale: Double = 0.0,    // 进出液速度比值(2byte)

    // 清洗参数
    val cleanOutFillSpeed: Double = 0.0,       // 出液蠕动泵填充速度(2byte)
    val cleanInFillSpeed: Double = 0.0,        // 进液蠕动泵填充速度(2byte)
    val cleanOutDrainSpeed: Double = 0.0,      // 出液蠕动泵排液速度(2byte)
    val cleanInDrainSpeed: Double = 0.0,       // 进液蠕动泵排液速度(2byte)
    val cleanOutFillTime: Int = 0,        // 出液蠕动泵填充时间(2byte)
    val cleanInFillTime: Int = 0,         // 进液蠕动泵填充时间(2byte)
    val cleanOutDrainTime: Int = 0,       // 出液蠕动泵排液时间(2byte)
    val cleanInDrainTime: Int = 0,        // 进液蠕动泵排液时间(2byte)
    val cleanEmptyTime: Int = 0,        // 进液蠕动泵排空时间(2byte)
    val cleanScale: Double = 0.0,      // 进出液速度比值(2byte)

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
    fun toTransfer(): ArgumentsTransfer {
        return ArgumentsTransfer(
            outFillSpeed = outFillSpeed,
            inFillSpeed = inFillSpeed,
            outDrainSpeed = outDrainSpeed,
            inDrainSpeed = inDrainSpeed,
            outFillTime = outFillTime,
            inFillTime = inFillTime,
            outDrainTime = outDrainTime,
            inDrainTime = inDrainTime,
            emptyTime = emptyTime,
            scale = scale
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
            cleanEmptyTime = cleanEmptyTime,
            cleanScale = cleanScale
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
}

data class ArgumentsTransfer(
    // 实验参数
    val outFillSpeed: Double = 0.0,       // 出液蠕动泵填充速度(2byte)
    val inFillSpeed: Double = 0.0,        // 进液蠕动泵填充速度(2byte)
    val outDrainSpeed: Double = 0.0,      // 出液蠕动泵排液速度(2byte)
    val inDrainSpeed: Double = 0.0,       // 进液蠕动泵排液速度(2byte)
    val outFillTime: Int = 0,        // 出液蠕动泵填充时间(2byte)
    val inFillTime: Int = 0,         // 进液蠕动泵填充时间(2byte)
    val outDrainTime: Int = 0,       // 出液蠕动泵排液时间(2byte)
    val inDrainTime: Int = 0,        // 进液蠕动泵排液时间(2byte)
    val emptyTime: Int = 0,        // 进液蠕动泵排空时间(2byte)
    val scale: Double = 0.0     // 进出液速度比值(2byte)
) {
    fun toByteArray(): ByteArray {
        val bytes = ByteArray(20)
        bytes.writeInt16LE((outFillSpeed * 100).toInt(), 0)
        bytes.writeInt16LE((inFillSpeed * 100).toInt(), 2)
        bytes.writeInt16LE((outDrainSpeed * 100).toInt(), 4)
        bytes.writeInt16LE((inDrainSpeed * 100).toInt(), 6)
        bytes.writeInt16LE(outFillTime  * 60, 8)
        bytes.writeInt16LE(inFillTime * 60, 10)
        bytes.writeInt16LE(outDrainTime * 60, 12)
        bytes.writeInt16LE(inDrainTime * 60, 14)
        bytes.writeInt16LE(emptyTime * 60, 16)
        bytes.writeInt16LE((scale * 100).toInt(), 18)
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
    val cleanEmptyTime: Int = 0,        // 进液蠕动泵排空时间(2byte)
    val cleanScale: Double = 0.0       // 进出液速度比值(2byte)
) {
    fun toByteArray(): ByteArray {
        val bytes = ByteArray(20)
        bytes.writeInt16LE((cleanOutFillSpeed * 100).toInt(), 0)
        bytes.writeInt16LE((cleanInFillSpeed * 100).toInt(), 2)
        bytes.writeInt16LE((cleanOutDrainSpeed * 100).toInt(), 4)
        bytes.writeInt16LE((cleanInDrainSpeed * 100).toInt(), 6)
        bytes.writeInt16LE(cleanOutFillTime * 60, 8)
        bytes.writeInt16LE(cleanInFillTime * 60, 10)
        bytes.writeInt16LE(cleanOutDrainTime * 60, 12)
        bytes.writeInt16LE(cleanInDrainTime * 60, 14)
        bytes.writeInt16LE(cleanEmptyTime * 60, 16)
        bytes.writeInt16LE((cleanScale * 100).toInt(), 18)
        return bytes
    }
}

data class ArgumentsTemperature(
    // 温度补偿参数
    val tempComp: List<Double> = List(10) { 0.0 }            // 温度补偿值(2byte)*10
) {
    fun toByteArray(): ByteArray {
        val bytes = ByteArray(20)
        for (i in tempComp.indices) {
            bytes.writeInt16LE((tempComp[i] * 100).toInt(), i * 2)
        }
        return bytes
    }
}

data class ArgumentsSpeed(
    // 速度补偿参数
    val outSpeedComp: List<Double> = List(10) { 0.0 },        // 出液转速补偿值(2byte)*10
    val inSpeedComp: List<Double> = List(10) { 0.0 }          // 进液转速补偿值(2byte)*10
) {
    fun toByteArray(): ByteArray {
        val bytes = ByteArray(40)
        for (i in outSpeedComp.indices) {
            bytes.writeInt16LE((outSpeedComp[i] * 100).toInt(), i * 2)
        }
        for (i in inSpeedComp.indices) {
            bytes.writeInt16LE((inSpeedComp[i] * 100).toInt(), i * 2 + 20)
        }
        return bytes
    }
}

data class ArgumentsVoltage(
    // 电压补偿参数
    val voltComp: List<Double> = List(10) { 0.0 }            // 电压补偿值(2byte)*10
) {
    fun toByteArray(): ByteArray {
        val bytes = ByteArray(20)
        for (i in voltComp.indices) {
            bytes.writeInt16LE((voltComp[i] * 100).toInt(), i * 2)
        }
        return bytes
    }
}

data class ArgumentsCurrent(
    // 电流补偿参数
    val currComp: List<Double> = List(10) { 0.0 }            // 电流补偿值(2byte)*10
) {
    fun toByteArray(): ByteArray {
        val bytes = ByteArray(20)
        for (i in currComp.indices) {
            bytes.writeInt16LE((currComp[i] * 100).toInt(), i * 2)
        }
        return bytes
    }
}

data class ArgumentsBubble(
    // 光耦阈值
    val outBubbleThreshold: Double = 0.0,    // 出液气泡光耦阈值(2byte)
    val inBubbleThreshold: Double = 0.0      // 进液气泡光耦阈值(2byte)
) {
    fun toByteArray(): ByteArray {
        val bytes = ByteArray(4)
        bytes.writeInt16LE((outBubbleThreshold * 100).toInt(), 0)
        bytes.writeInt16LE((inBubbleThreshold * 100).toInt(), 2)
        return bytes
    }
}

// bytes to Arguments
fun toArguments(bytes: ByteArray): Arguments? {
    if (bytes.size != 144) return null
    return Arguments(
        outFillSpeed = bytes.readInt16LE(0) / 100.0,
        inFillSpeed = bytes.readInt16LE(2) / 100.0,
        outDrainSpeed = bytes.readInt16LE(4) / 100.0,
        inDrainSpeed = bytes.readInt16LE(6) / 100.0,
        outFillTime = bytes.readInt16LE(8) / 60,
        inFillTime = bytes.readInt16LE(10) / 60,
        outDrainTime = bytes.readInt16LE(12) / 60,
        inDrainTime = bytes.readInt16LE(14) / 60,
        emptyTime = bytes.readInt16LE(16) / 60,
        scale = bytes.readInt16LE(18) / 100.0,
        cleanOutFillSpeed = bytes.readInt16LE(20) / 100.0,
        cleanInFillSpeed = bytes.readInt16LE(22) / 100.0,
        cleanOutDrainSpeed = bytes.readInt16LE(24) / 100.0,
        cleanInDrainSpeed = bytes.readInt16LE(26) / 100.0,
        cleanOutFillTime = bytes.readInt16LE(28) / 60,
        cleanInFillTime =   bytes.readInt16LE(30) / 60,
        cleanOutDrainTime = bytes.readInt16LE(32) / 60,
        cleanInDrainTime = bytes.readInt16LE(34) / 60,
        cleanEmptyTime = bytes.readInt16LE(36) / 60,
        cleanScale = bytes.readInt16LE(38) / 100.0,
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