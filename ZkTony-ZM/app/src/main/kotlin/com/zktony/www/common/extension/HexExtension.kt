package com.zktony.www.common.extension

import java.math.BigInteger

/**
 * 8比特的int转1位Hex
 * @return Hex x 2 like "AA55" "0102"
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


