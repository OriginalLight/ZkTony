package com.zktony.serialport.command

import com.zktony.serialport.ext.crc16
import com.zktony.serialport.ext.replaceByteArrayBE
import com.zktony.serialport.ext.writeInt16BE

class Protocol {
    var head: Byte = 0xEE.toByte()
    var addr: Byte = 0x01.toByte()
    var fn: Byte = 0x01.toByte()
    var len: ByteArray = byteArrayOf(0x00, 0x00)
    var data: ByteArray = byteArrayOf()
    var crc: ByteArray = byteArrayOf(0x00, 0x00)
    var end: Byte = 0xBB.toByte()

    fun toByteArray(): ByteArray {
        val array = byteArrayOf(head, addr, fn) + len.writeInt16BE(data.size, 0) + data + crc + end
        val crcArray = array.copyOfRange(0, array.size - 3).crc16()
        return array.replaceByteArrayBE(crcArray, array.size - 3, 0)
    }
}

fun protocol(block: Protocol.() -> Unit): Protocol {
    return Protocol().apply(block)
}

fun protocol(data: ByteArray): Protocol {
    return protocol {
        head = data[0]
        addr = data[1]
        fn = data[2]
        len = data.copyOfRange(3, 5)
        this.data = data.copyOfRange(5, data.size - 3)
        crc = data.copyOfRange(data.size - 3, data.size - 1)
        end = data[data.size - 1]
    }
}