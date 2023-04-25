package com.zktony.serialport.protocol

import com.zktony.serialport.ext.CRC16

data class V2(
    var head: String = "EE",
    // 01 上位机 02 下位机
    var addr: String = "01",
    // 01 运动 02 停止
    var fn: String = "01",
    // data
    var data: String = "",
    var crc: String = "",
    var end: String = "BB"
) {
    fun toHex(): String {
        val crc = CRC16.getCRC(head + addr + fn + data)
        return head + addr + fn + data + crc + end
    }
}

fun v2(block: V2.() -> Unit): String {
    return V2().apply(block).toHex()
}
