package com.zktony.serialport.command

import com.zktony.serialport.ext.crc16LE
import com.zktony.serialport.ext.replaceByteArrayBE
import com.zktony.serialport.ext.writeInt16LE

class Protocol {
    var head: Byte = 0xEE.toByte()
    var id: Byte = 0x01.toByte()
    var cmd: Byte = 0x01.toByte()
    var len: ByteArray = byteArrayOf(0x00.toByte(), 0x00.toByte())
    var data: ByteArray = byteArrayOf()
    var crc: ByteArray = byteArrayOf(0x00.toByte(), 0x00.toByte())
    var end: ByteArray = byteArrayOf(0xFF.toByte(), 0xFC.toByte(), 0xFF.toByte(), 0xFF.toByte())

    fun toByteArray(): ByteArray {
        val byteArray = byteArrayOf(head, id, cmd)
            .plus(len.writeInt16LE(data.size, 0))
            .plus(data)
            .plus(crc)
            .plus(end)


        // crc
        return byteArray.replaceByteArrayBE(
            byteArray.copyOfRange(0, byteArray.size - 6).crc16LE(),
            byteArray.size - 6,
            0
        )
    }
}

fun protocol(block: Protocol.() -> Unit): Protocol {
    return Protocol().apply(block)
}

fun ByteArray.protocol(): Protocol {
    val bytes = this
    return protocol {
        head = bytes[0]
        id = bytes[1]
        cmd = bytes[2]
        len = bytes.copyOfRange(3, 5)
        data = bytes.copyOfRange(5, bytes.size - 6)
        crc = bytes.copyOfRange(bytes.size - 6, bytes.size - 4)
        end = bytes.copyOfRange(bytes.size - 4, bytes.size)
    }
}