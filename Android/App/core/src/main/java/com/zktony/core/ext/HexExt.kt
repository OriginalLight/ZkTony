package com.zktony.core.ext

import java.math.BigInteger

/**
 * 8比特的int转1位Hex
 * @return Hex x 1 like "AA" "01"
 */
fun Int.intToHex(): String {
    val result = Integer.toHexString(this).uppercase()
    return if (result.length % 2 == 1) "0$result" else result
}

/**
 * 16比特的int转2位Hex
 * @return Hex x 1 like "AA55" "0102"
 */
fun Int.intToHex2(): String {
    val result = Integer.toHexString(this).uppercase()
    return when {
        result.length < 4 -> "0".repeat(4 - result.length) + result
        else -> result
    }
}

/**
 * 32比特的int转4位Hex
 * @return Hex x 4 like "AA55AA55" "01020102"
 */
fun Int.intToHex4(): String {
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
fun String.hexToInt(): Int {
    return BigInteger(this.replace(" ", ""), 16).toInt()
}

/**
 * 32比特的float转4位Hex
 * @return Hex x 4 like "000000AA" "00000001"
 */
fun Float.floatToHex4(): String {
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
fun String.hex4ToFloat(): Float {
    return java.lang.Float.intBitsToFloat(BigInteger(this.trim(), 16).toInt())
}

/**
 * hex 高低位转换
 * 例如 000000AA -> AA000000
 * @return Hex x 4 like "000000AA" "00000001"
 */
fun String.hexHighLow(): String {
    val str = this.replace(" ", "")
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
 * hex命令格式化每两个字符加空格
 *
 * @receiver String
 * @return String
 */
fun String.hexFormat(): String {
    // 去掉空格
    val str = this.replace(" ", "")
    val result = StringBuilder()
    for (i in 0 until str.length / 2) {
        result.append(str.substring(2 * i, 2 * i + 2)).append(" ")
    }
    return result.toString()
}

/**
 * hexToAscii
 *
 * @receiver String
 * @return String
 */
fun String.hexToAscii(): String {
    val hexStr = this.replace(" ", "")
    val output = StringBuilder("")
    var i = 0
    while (i < hexStr.length) {
        val str = hexStr.substring(i, i + 2)
        output.append(str.toInt(16).toChar())
        i += 2
    }
    return output.toString()
}



