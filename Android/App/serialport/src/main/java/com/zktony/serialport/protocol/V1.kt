package com.zktony.serialport.protocol


/**
 * @author: 刘贺贺
 * @date: 2022-10-17 13:09
 */
data class V1(
    var head: String = "EE",
    var addr: String = "01",
    var fn: String = "06",
    var pa: String = "0A",
    var data: String = "",
    var end: String = "FFFCFFFF"
) {
    /**
     * 获取十六进制字符串
     * @return 16进制字符串
     */
    fun toHex(): String {
        return head + addr + fn + pa + data + end
    }
}

fun v1(block: V1.() -> Unit): String {
    return V1().apply(block).toHex()
}