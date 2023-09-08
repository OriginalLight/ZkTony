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

/**
 * 解析十六进制字符串为V1
 * @return [V1]
 */
fun String.toV1(): V1? {
    if (isEmpty() || !startsWith("EE") || !endsWith("FFFCFFFF") || length % 2 != 0 ) {
        return null
    }
    return V1(
        head = this.substring(0, 2),
        addr = this.substring(2, 4),
        fn = this.substring(4, 6),
        pa = this.substring(6, 8),
        data = this.substring(8, this.length - 8),
        end = this.substring(this.length - 8, this.length)
    )
}