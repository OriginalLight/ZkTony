package com.zktony.serialport.protocol

import com.zktony.serialport.ext.crc16
import com.zktony.serialport.ext.intToHex


data class V2(
    var head: String = "EE",
    // 01 上位机 02 下位机
    var addr: String = "01",
    // 01 运动 02 停止
    var fn: String = "01",
    // len
    var len: String = "0000",
    // data
    var data: String = "",
    // crc 校验
    var crc: String = "0000",
    // 结束符
    var end: String = "BB"
) {
    /**
     * 转换为16进制字符串
     *
     * @return String
     */
    fun toHex(): String {
        val crcArray = head + addr + fn + (data.length / 2).intToHex(2) + data
        val crc = crcArray.crc16()
        return crcArray + crc + end
    }
}

/**
 * 生成V2协议
 * @param block [@kotlin.ExtensionFunctionType] Function1<V2, Unit>
 * @return String
 */
fun v2(block: V2.() -> Unit): String {
    return V2().apply(block).toHex()
}

/**
 * 生成V2协议
 *
 * @receiver String
 * @return V2?
 */
fun String.toV2(): V2? {
    if (isEmpty() || !startsWith("EE") || !endsWith("BB") || length % 2 != 0) {
        return null
    }
    return V2(
        head = this.substring(0, 2),
        addr = this.substring(2, 4),
        fn = this.substring(4, 6),
        len = this.substring(6, 10),
        data = this.substring(10, this.length - 6),
        crc = this.substring(this.length - 6, this.length - 2),
        end = this.substring(this.length - 2)
    )
}