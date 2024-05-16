package com.zktony.serialport.ext

/**
 * @author 刘贺贺
 * @date 2023/8/24 14:54
 */

/**
 * 累加和校验
 *
 * @receiver ByteArray
 * @return Int
 */
fun ByteArray.checkSum(): Int {
    var sum = 0
    for (j in this) {
        sum += j.toInt() and 0xff
    }
    return sum
}


fun ByteArray.checkSumBE(): ByteArray {
    val sum: Int = this.checkSum()
    return byteArrayOf((sum shr 8 and 0xff).toByte(), (sum and 0xff).toByte())
}

fun ByteArray.checkSumLE(): ByteArray {
    val sum: Int = this.checkSum()
    return byteArrayOf((sum and 0xff).toByte(), (sum shr 8 and 0xff).toByte())
}