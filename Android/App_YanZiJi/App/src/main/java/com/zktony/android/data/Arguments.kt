package com.zktony.android.data

import com.zktony.log.LogUtils
import com.zktony.serialport.ext.readInt16LE
import com.zktony.serialport.ext.writeInt16LE
import com.zktony.serialport.ext.writeInt8
import kotlinx.serialization.Serializable
import java.math.RoundingMode

@Serializable
data class Arguments(
    // 实验参数
    val outFillSpeed: String = "0",       // 出液蠕动泵填充速度(2byte)
    val inFillSpeed: String = "0",        // 进液蠕动泵填充速度(2byte)
    val outDrainSpeed: String = "0",      // 出液蠕动泵排液速度(2byte)
    val inDrainSpeed: String = "0",       // 进液蠕动泵排液速度(2byte)
    val outFillTime: String = "0",          // 出液蠕动泵填充时间(2byte)
    val inFillTime: String = "0",           // 进液蠕动泵填充时间(2byte)
    val outDrainTime: String = "0",         // 出液蠕动泵排液时间(2byte)
    val inDrainTime: String = "0",          // 进液蠕动泵排液时间(2byte)
    val emptyTime: String = "0",            // 进液蠕动泵排空时间(2byte)
    val scale: String = "0",              // 进出液速度比值(2byte)

    // 清洗参数
    val cleanOutFillSpeed: String = "0",       // 出液蠕动泵填充速度(2byte)
    val cleanInFillSpeed: String = "0",        // 进液蠕动泵填充速度(2byte)
    val cleanOutDrainSpeed: String = "0",      // 出液蠕动泵排液速度(2byte)
    val cleanInDrainSpeed: String = "0",       // 进液蠕动泵排液速度(2byte)
    val cleanOutFillTime: String = "0",          // 出液蠕动泵填充时间(2byte)
    val cleanInFillTime: String = "0",           // 进液蠕动泵填充时间(2byte)
    val cleanOutDrainTime: String = "0",         // 出液蠕动泵排液时间(2byte)
    val cleanInDrainTime: String = "0",          // 进液蠕动泵排液时间(2byte)
    val cleanEmptyTime: String = "0",            // 进液蠕动泵排空时间(2byte)
    val cleanScale: String = "0",              // 进出液速度比值(2byte)

    // 补偿参数
    val tempComp: List<String> = List(10) { "0" },            // 温度补偿值(2byte)*10
    val outSpeedComp: List<String> = List(10) { "0" },        // 出液转速补偿值(2byte)*10
    val inSpeedComp: List<String> = List(10) { "0" },         // 进液转速补偿值(2byte)*10
    val voltComp: List<String> = List(10) { "0" },            // 电压补偿值(2byte)*10
    val currComp: List<String> = List(10) { "0" },            // 电流补偿值(2byte)*10

    // 光耦阈值
    val outBubbleThreshold: String = "0",    // 出液气泡光耦阈值(2byte)
    val inBubbleThreshold: String = "0"      // 进液气泡光耦阈值(2byte)
) {
    fun toByteArray(): ByteArray {
        val bytes = ByteArray(144)
        bytes.writeInt16LE(outFillSpeed.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 0)
        bytes.writeInt16LE(inFillSpeed.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 2)
        bytes.writeInt16LE(outDrainSpeed.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 4)
        bytes.writeInt16LE(inDrainSpeed.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 6)
        bytes.writeInt16LE(outFillTime.toBigDecimal().toInt(), 8)
        bytes.writeInt16LE(inFillTime.toBigDecimal().toInt(), 10)
        bytes.writeInt16LE(outDrainTime.toBigDecimal().toInt(), 12)
        bytes.writeInt16LE(inDrainTime.toBigDecimal().toInt(), 14)
        bytes.writeInt16LE(emptyTime.toBigDecimal().toInt(), 16)
        bytes.writeInt16LE(scale.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 18)
        bytes.writeInt16LE(
            cleanOutFillSpeed.toBigDecimal().multiply(100.toBigDecimal()).toInt(),
            20
        )
        bytes.writeInt16LE(cleanInFillSpeed.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 22)
        bytes.writeInt16LE(
            cleanOutDrainSpeed.toBigDecimal().multiply(100.toBigDecimal()).toInt(),
            24
        )
        bytes.writeInt16LE(
            cleanInDrainSpeed.toBigDecimal().multiply(100.toBigDecimal()).toInt(),
            26
        )
        bytes.writeInt16LE(cleanOutFillTime.toBigDecimal().toInt(), 28)
        bytes.writeInt16LE(cleanInFillTime.toBigDecimal().toInt(), 30)
        bytes.writeInt16LE(cleanOutDrainTime.toBigDecimal().toInt(), 32)
        bytes.writeInt16LE(cleanInDrainTime.toBigDecimal().toInt(), 34)
        bytes.writeInt16LE(cleanEmptyTime.toBigDecimal().toInt(), 36)
        bytes.writeInt16LE(cleanScale.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 38)
        for (i in tempComp.indices) {
            val value = tempComp[i].toBigDecimal().subtract((10 * (i + 1)).toBigDecimal())
                .multiply(100.toBigDecimal()).toInt()
            bytes.writeInt16LE(value, i * 2 + 40)
        }
        for (i in inSpeedComp.indices) {
            val value = inSpeedComp[i].toBigDecimal().multiply(100.toBigDecimal()).toInt()
            bytes.writeInt16LE(value, i * 2 + 80)
        }
        for (i in outSpeedComp.indices) {
            val value = outSpeedComp[i].toBigDecimal().multiply(100.toBigDecimal()).toInt()
            bytes.writeInt16LE(value, i * 2 + 60)
        }
        for (i in voltComp.indices) {
            val value = voltComp[i].toBigDecimal().subtract((5 * (i + 1)).toBigDecimal())
                .multiply(100.toBigDecimal()).toInt()
            bytes.writeInt16LE(value, i * 2 + 100)
        }

        for (i in currComp.indices) {
            val value = currComp[i].toBigDecimal().subtract((0.5 * (i + 1)).toBigDecimal())
                .multiply(100.toBigDecimal()).toInt()
            bytes.writeInt16LE(value, i * 2 + 120)
        }

        bytes.writeInt16LE(
            outBubbleThreshold.toBigDecimal().multiply(100.toBigDecimal()).toInt(),
            140
        )
        bytes.writeInt16LE(
            inBubbleThreshold.toBigDecimal().multiply(100.toBigDecimal()).toInt(),
            142
        )

        return bytes
    }
}

data class ArgumentsTransfer(
    // 实验参数
    val outFillSpeed: String = "0",           // 出液蠕动泵填充速度(2byte)
    val inFillSpeed: String = "0",            // 进液蠕动泵填充速度(2byte)
    val outDrainSpeed: String = "0",          // 出液蠕动泵排液速度(2byte)
    val inDrainSpeed: String = "0",           // 进液蠕动泵排液速度(2byte)
    val outFillTime: String = "0",              // 出液蠕动泵填充时间(2byte)
    val inFillTime: String = "0",               // 进液蠕动泵填充时间(2byte)
    val outDrainTime: String = "0",             // 出液蠕动泵排液时间(2byte)
    val inDrainTime: String = "0",              // 进液蠕动泵排液时间(2byte)
    val emptyTime: String = "0",                // 进液蠕动泵排空时间(2byte)
    val scale: String = "0"                   // 进出液速度比值(2byte)
) {
    fun toByteArray(): ByteArray {
        val bytes = ByteArray(20)
        bytes.writeInt16LE(outFillSpeed.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 0)
        bytes.writeInt16LE(inFillSpeed.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 2)
        bytes.writeInt16LE(outDrainSpeed.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 4)
        bytes.writeInt16LE(inDrainSpeed.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 6)
        bytes.writeInt16LE(outFillTime.toBigDecimal().toInt(), 8)
        bytes.writeInt16LE(inFillTime.toBigDecimal().toInt(), 10)
        bytes.writeInt16LE(outDrainTime.toBigDecimal().toInt(), 12)
        bytes.writeInt16LE(inDrainTime.toBigDecimal().toInt(), 14)
        bytes.writeInt16LE(emptyTime.toBigDecimal().toInt(), 16)
        bytes.writeInt16LE(scale.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 18)
        return bytes
    }
}

data class ArgumentsClean(
    // 清洗参数
    val cleanOutFillSpeed: String = "0",       // 出液蠕动泵填充速度(2byte)
    val cleanInFillSpeed: String = "0",        // 进液蠕动泵填充速度(2byte)
    val cleanOutDrainSpeed: String = "0",      // 出液蠕动泵排液速度(2byte)
    val cleanInDrainSpeed: String = "0",       // 进液蠕动泵排液速度(2byte)
    val cleanOutFillTime: String = "0",          // 出液蠕动泵填充时间(2byte)
    val cleanInFillTime: String = "0",           // 进液蠕动泵填充时间(2byte)
    val cleanOutDrainTime: String = "0",         // 出液蠕动泵排液时间(2byte)
    val cleanInDrainTime: String = "0",          // 进液蠕动泵排液时间(2byte)
    val cleanEmptyTime: String = "0",            // 进液蠕动泵排空时间(2byte)
    val cleanScale: String = "0"               // 进出液速度比值(2byte)
) {
    fun toByteArray(): ByteArray {
        val bytes = ByteArray(20)
        bytes.writeInt16LE(cleanOutFillSpeed.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 0)
        bytes.writeInt16LE(cleanInFillSpeed.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 2)
        bytes.writeInt16LE(
            cleanOutDrainSpeed.toBigDecimal().multiply(100.toBigDecimal()).toInt(),
            4
        )
        bytes.writeInt16LE(cleanInDrainSpeed.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 6)
        bytes.writeInt16LE(cleanOutFillTime.toBigDecimal().toInt(), 8)
        bytes.writeInt16LE(cleanInFillTime.toBigDecimal().toInt(), 10)
        bytes.writeInt16LE(cleanOutDrainTime.toBigDecimal().toInt(), 12)
        bytes.writeInt16LE(cleanInDrainTime.toBigDecimal().toInt(), 14)
        bytes.writeInt16LE(cleanEmptyTime.toBigDecimal().toInt(), 16)
        bytes.writeInt16LE(cleanScale.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 18)
        return bytes
    }
}

data class ArgumentsTemperature(
    // 温度补偿参数
    val tempComp: List<String> = List(10) { "0" }            // 温度补偿值(2byte)*10
) {
    fun toByteArray(): ByteArray {
        val bytes = ByteArray(20)
        for (i in tempComp.indices) {
            val value = tempComp[i].toBigDecimal().subtract((10 * (i + 1)).toBigDecimal())
                .multiply(100.toBigDecimal()).toInt()
            bytes.writeInt16LE(value, i * 2)
        }
        return bytes
    }
}

data class ArgumentsSpeed(
    // 速度补偿参数
    val inOrOut: Int = 0,                                          // 进液体还是出液体
    val speedComp: List<String> = List(10) { "0" }          // 进液转速补偿值(2byte)*10
) {
    fun toByteArray(): ByteArray {
        val bytes = ByteArray(21)
        bytes.writeInt8(inOrOut)
        for (i in speedComp.indices) {
            val value = speedComp[i].toBigDecimal().multiply(100.toBigDecimal()).toInt()
            bytes.writeInt16LE(value, i * 2 + 1)
        }
        return bytes
    }
}

data class ArgumentsVoltage(
    // 电压补偿参数
    val voltComp: List<String> = List(10) { "0" }            // 电压补偿值(2byte)*10
) {
    fun toByteArray(): ByteArray {
        val bytes = ByteArray(20)
        for (i in voltComp.indices) {
            val value = voltComp[i].toBigDecimal().subtract((5 * (i + 1)).toBigDecimal())
                .multiply(100.toBigDecimal()).toInt()
            bytes.writeInt16LE(value, i * 2)
        }
        return bytes
    }
}

data class ArgumentsCurrent(
    // 电流补偿参数
    val currComp: List<String> = List(10) { "0" }            // 电流补偿值(2byte)*10
) {
    fun toByteArray(): ByteArray {
        val bytes = ByteArray(20)
        for (i in currComp.indices) {
            val value = currComp[i].toBigDecimal().subtract((0.5 * (i + 1)).toBigDecimal())
                .multiply(100.toBigDecimal()).toInt()
            bytes.writeInt16LE(value, i * 2)
        }
        return bytes
    }
}

data class ArgumentsBubble(
    // 光耦阈值
    val outBubbleThreshold: String = "0",    // 出液气泡光耦阈值(2byte)
    val inBubbleThreshold: String = "0"      // 进液气泡光耦阈值(2byte)
) {
    fun toByteArray(): ByteArray {
        val bytes = ByteArray(4)
        bytes.writeInt16LE(
            outBubbleThreshold.toBigDecimal().multiply(100.toBigDecimal()).toInt(),
            0
        )
        bytes.writeInt16LE(inBubbleThreshold.toBigDecimal().multiply(100.toBigDecimal()).toInt(), 2)
        return bytes
    }
}

// bytes to Arguments
fun toArguments(bytes: ByteArray): Arguments? {
    try {
        if (bytes.size != 144) throw Exception("Arguments 长度不正确")
        return Arguments(
            outFillSpeed = bytes.readInt16LE(0).toBigDecimal()
                .divide(100.toBigDecimal(), RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString(),
            inFillSpeed = bytes.readInt16LE(2).toBigDecimal()
                .divide(100.toBigDecimal(), RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString(),
            outDrainSpeed = bytes.readInt16LE(4).toBigDecimal()
                .divide(100.toBigDecimal(), RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString(),
            inDrainSpeed = bytes.readInt16LE(6).toBigDecimal()
                .divide(100.toBigDecimal(), RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString(),
            outFillTime = bytes.readInt16LE(8).toBigDecimal()
                .stripTrailingZeros().toPlainString(),
            inFillTime = bytes.readInt16LE(10).toBigDecimal()
                .stripTrailingZeros().toPlainString(),
            outDrainTime = bytes.readInt16LE(12).toBigDecimal()
                .stripTrailingZeros().toPlainString(),
            inDrainTime = bytes.readInt16LE(14).toBigDecimal()
                .stripTrailingZeros().toPlainString(),
            emptyTime = bytes.readInt16LE(16).toBigDecimal()
                .stripTrailingZeros().toPlainString(),
            scale = bytes.readInt16LE(18).toBigDecimal()
                .divide(100.toBigDecimal(), RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString(),
            cleanOutFillSpeed = bytes.readInt16LE(20).toBigDecimal()
                .divide(100.toBigDecimal(), RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString(),
            cleanInFillSpeed = bytes.readInt16LE(22).toBigDecimal()
                .divide(100.toBigDecimal(), RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString(),
            cleanOutDrainSpeed = bytes.readInt16LE(24).toBigDecimal()
                .divide(100.toBigDecimal(), RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString(),
            cleanInDrainSpeed = bytes.readInt16LE(26).toBigDecimal()
                .divide(100.toBigDecimal(), RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString(),
            cleanOutFillTime = bytes.readInt16LE(28).toBigDecimal()
                .stripTrailingZeros().toPlainString(),
            cleanInFillTime = bytes.readInt16LE(30).toBigDecimal()
                .stripTrailingZeros().toPlainString(),
            cleanOutDrainTime = bytes.readInt16LE(32).toBigDecimal()
                .stripTrailingZeros().toPlainString(),
            cleanInDrainTime = bytes.readInt16LE(34).toBigDecimal()
                .stripTrailingZeros().toPlainString(),
            cleanEmptyTime = bytes.readInt16LE(36).toBigDecimal()
                .stripTrailingZeros().toPlainString(),
            cleanScale = bytes.readInt16LE(38).toBigDecimal()
                .divide(100.toBigDecimal(), RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString(),
            tempComp = toStringList(bytes.copyOfRange(40, 60), 10.0),
            inSpeedComp = toStringList(bytes.copyOfRange(60, 80), 0.0),
            outSpeedComp = toStringList(bytes.copyOfRange(80, 100), 0.0),
            voltComp = toStringList(bytes.copyOfRange(100, 120), 5.0),
            currComp = toStringList(bytes.copyOfRange(120, 140), 0.5),
            outBubbleThreshold = bytes.readInt16LE(140).toBigDecimal()
                .divide(100.toBigDecimal(), RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString(),
            inBubbleThreshold = bytes.readInt16LE(142).toBigDecimal()
                .divide(100.toBigDecimal(), RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString()
        )
    } catch (e: Exception) {
        LogUtils.error("Arguments 解析失败: ${e.printStackTrace()}", true)
        return null
    }
}

private fun toStringList(bytes: ByteArray, offset: Double): List<String> {
    val list = mutableListOf<String>()
    for (i in bytes.indices step 2) {
        val value =
            bytes.readInt16LE(i).toBigDecimal().divide(100.toBigDecimal(), RoundingMode.HALF_UP)
                .add((offset * (i / 2 + 1)).toBigDecimal()).stripTrailingZeros().toPlainString()
        list.add(value)
    }
    return list
}