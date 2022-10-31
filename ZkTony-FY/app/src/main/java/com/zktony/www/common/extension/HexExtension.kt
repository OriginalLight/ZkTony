package com.zktony.www.common.extension

import com.zktony.www.common.room.entity.Motor
import com.zktony.www.serialport.protocol.Command
import java.math.BigInteger

/**
 * 8比特的int转1位Hex
 * @return Hex x 1 like "AA" "01"
 */
fun Int.int8ToHex(): String {
    val result = Integer.toHexString(this).uppercase()
    return if (result.length % 2 == 1) "0$result" else result
}


/**
 * 1位的hex转8比特的int
 * @return int
 */
fun String.hexToInt8(): Int {
    return BigInteger(this.trim(), 16).toInt()
}

/**
 * 16比特的int转2位Hex
 * @return Hex x 1 like "AA55" "0102"
 */
fun Int.int16ToHex2(): String {
    val result = Integer.toHexString(this).uppercase()
    return when {
        result.length < 4 -> "0".repeat(4 - result.length) + result
        else -> result
    }
}

/**
 * 2位的hex转16比特的int
 * @return int
 */
fun String.hex2ToInt16(): Int {
    return BigInteger(this.trim(), 16).toInt()
}

/**
 * 32比特的int转4位Hex
 * @return Hex x 4 like "AA55AA55" "01020102"
 */
fun Int.int32ToHex4(): String {
    val result = Integer.toHexString(this).uppercase()
    return when {
        result.length < 8 -> "0".repeat(8 - result.length) + result
        else -> result
    }
}

/**
 * 4位Hex转32比特的int
 * @return int
 */
fun String.hex4ToInt32(): Int {
    return BigInteger(this.trim(), 16).toInt()
}

/**
 * 32比特的float转4位Hex
 * @return Hex x 4 like "000000AA" "00000001"
 */
fun Float.float32ToHex4(): String {
    val result = Integer.toHexString(java.lang.Float.floatToIntBits(this)).uppercase()
    return when {
        result.length < 8 -> "0".repeat(8 - result.length) + result
        else -> result
    }
}

/**
 * 4位的hex转32比特的float
 * @return float
 */
fun String.hex4ToFloat32(): Float {
    return java.lang.Float.intBitsToFloat(BigInteger(this.trim(), 16).toInt())
}

/**
 * hex 高低位转换
 * 例如 000000AA -> AA000000
 * @return Hex x 4 like "000000AA" "00000001"
 */
fun String.hexHighLow(): String {
    val str = this.trim()
    val result = StringBuilder()
    for (i in 0 until str.length / 2) {
        result.append(str.substring(str.length - 2 * (i + 1), str.length - 2 * i))
    }
    return result.toString()
}

/**
 * 字符串转HEx
 */
fun String.toHex(): String {
    val byteArray = this.toByteArray()
    val sb = StringBuilder()
    for (i in byteArray.indices) {
        sb.append(byteArray[i].toInt().and(0xFF).toString(16))
    }
    return sb.toString()
}

/**
 * hex命令分割
 * 将hex中所有以EE开头并且以FFFCFFFF结尾的命令放进数组中
 * @return List<String>
 */
fun String.splitHex(): List<String> {
    // 在FFFCFFFF后面加空格
    val str = this.replace("FFFCFFFF", "FFFCFFFF，")
    // 使用空格分割字符串
    return str.split("，")
}

/**
 * hex命令查找某个字符的个数
 */
fun String.hexCount(str: String): Int {
    var count = 0
    var index = 0
    while (index != -1) {
        index = this.indexOf(str, index)
        if (index != -1) {
            count++
            index += str.length
        }
    }
    return count
}

/**
 * 验证Hex
 * 不为空
 * 以EE开头并且以FFFCFFFF结尾
 * 如果FFFCFFFF的个数大于1，分割hex
 */
fun String.verifyHex(): List<String> {
    if (this.isEmpty()) {
        return emptyList()
    }
    if (!this.startsWith("EE") || !this.endsWith("FFFCFFFF")) {
        return emptyList()
    }
    if (this.hexCount("FFFCFFFF") > 1) {
        return this.splitHex().filter { it.isNotEmpty() }
    }
    return listOf(this)
}

/**
 * hex命令格式化每两个字符加空格
 */
fun String.hexFormat(): String {
    val str = this.trim()
    val result = StringBuilder()
    for (i in 0 until str.length / 2) {
        result.append(str.substring(2 * i, 2 * i + 2)).append(" ")
    }
    return result.toString()
}

/**
 * 解析电机数据
 * @return [Motor] 电机
 */
fun String.toMotor(): Motor {
    return Motor(
        address = this.substring(0, 2).hexToInt8(),
        subdivision = this.substring(2, 4).hexToInt8(),
        speed = this.substring(4, 8).hex2ToInt16(),
        acceleration = this.substring(8, 10).hexToInt8(),
        deceleration = this.substring(10, 12).hexToInt8(),
        mode = this.substring(12, 14).hexToInt8(),
        waitTime = this.substring(14, 18).hex2ToInt16()
    )
}

/**
 * 解析十六进制字符串为Command
 * @return [Command]
 */
fun String.toCommand(): Command {
    return Command(
        header = this.substring(0, 2),
        address = this.substring(2, 4),
        function = this.substring(4, 6),
        parameter = this.substring(6, 8),
        data = this.substring(8, this.length - 8),
        end = this.substring(this.length - 8, this.length)
    )
}


