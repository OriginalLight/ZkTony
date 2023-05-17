package com.zktony.serialport.command

import com.zktony.serialport.ext.crc16

class Protocol {
    var head: Byte = 0xEE.toByte()
    var addr: Byte = 0x01.toByte()
    var fn: Byte = 0x01.toByte()
    var len: ByteArray = byteArrayOf(0x00, 0x00)
    var data: ByteArray = byteArrayOf()
    var end: Byte = 0xBB.toByte()

    fun toByteArray(): ByteArray {
        val crcArray = byteArrayOf(head, addr, fn) + data
        val crc = crcArray.crc16()
        return crcArray + crc + end
    }
}

fun protocol(block: Protocol.() -> Unit): ByteArray {
    return Protocol().apply(block).toByteArray()
}