package com.zktony.www.manager.protocol

import com.zktony.core.ext.int8ToHex


data class V2(
    val header: String = "EE",
    val addr: String = "01",
    val fn: String = "06",
    val pa: String = "0A",
    val body: Body = Body(),
    val end: String = "FFFCFFFF"
) {
    /**
     * 获取十六进制字符串
     * @return 16进制字符串
     */
    fun toHex(): String {
        return header + addr + fn + pa + body.toHex() + end
    }
}


data class Body(
    val addr: List<Int> = emptyList(),
    val speed: List<Int> = emptyList(),
) {

    fun toHex(): String {
        val sb = StringBuilder()
        addr.forEach {
            sb.append(it.int8ToHex())
        }
        return sb.toString()
    }
}