package com.zktony.serialport.ext

import java.math.BigInteger

/**
 * 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
 *
 * @return 0 偶数 1 奇数
 */
fun Int.isOdd(): Int {
    return this and 0x1
}

/**
 * int转Hex
 *
 * @receiver Int
 * @param len Int
 * @return String
 */
fun Int.intToHex(len: Int = 1): String {
    val result = Integer.toHexString(this).uppercase()
    return when {
        result.length < len * 2 -> "0".repeat(len * 2 - result.length) + result
        else -> result
    }
}

/**
 * Hex转比特的int
 * @return int
 */
fun String.hexToInt(): Int {
    return BigInteger(this.trim(), 16).toInt()
}

/**
 * 32比特的float转4位Hex
 * @return Hex x 4 like "000000AA" "00000001"
 */
fun Float.floatToHex(): String {
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
fun String.hexToFloat(): Float {
    return java.lang.Float.intBitsToFloat(BigInteger(this.trim(), 16).toInt())
}

/**
 * 64比特的double转8位Hex
 * @return Hex x 8 like "00000000000000AA" "0000000000000001"
 */
fun Double.doubleToHex(): String {
    val result = java.lang.Double.doubleToLongBits(this).toString(16).uppercase()
    return when {
        result.length < 16 -> "0".repeat(16 - result.length) + result
        else -> result
    }
}

/**
 * 8位的hex转64比特的double
 * @return double
 */
fun String.hexToDouble(): Double {
    return java.lang.Double.longBitsToDouble(BigInteger(this.trim(), 16).toLong())
}

/**
 * Byte转Hex
 *
 * @receiver Byte
 * @return String
 */
fun Byte.byteToHex() = String.format("%02X", this)

/**
 * 一位Hex转byte
 *
 * @receiver String
 * @return Byte
 */
fun String.hexToByte() = this.toInt(16).toByte()

/**
 * 字节数组转十六进制
 *
 * @receiver ByteArray
 * @return String
 */
fun ByteArray.byteArrayToHexString() = joinToString("") { "%02X".format(it) }

/**
 * 十六进制转字符数组
 *
 * @receiver String
 * @return ByteArray
 */
fun String.hexStringToByteArray() =
    ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }

/**
 * asciiToHex
 */
fun String.asciiToHex(): String {
    val byteArray = this.toByteArray()
    val sb = StringBuilder()
    for (i in byteArray.indices) {
        sb.append(byteArray[i].toInt().and(0xFF).toString(16))
    }
    return sb.toString().uppercase()
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
 * 字符串分割
 *
 * @receiver String
 * @param head String
 * @param end String
 * @return List<String>
 */
fun String.splitString(head: String, end: String): List<String> {
    val str = this
    val list = ArrayList<String>()
    var index = 0
    while (index < str.length) {
        val start = str.indexOf(head, index)
        if (start == -1) {
            list.add(str)
            break
        }
        val stop = str.indexOf(end, start + 1)
        if (stop == -1) {
            list.add(str)
            break
        }
        list.add(str.substring(start, stop + end.length))
        index = stop + end.length
    }
    return list
}
