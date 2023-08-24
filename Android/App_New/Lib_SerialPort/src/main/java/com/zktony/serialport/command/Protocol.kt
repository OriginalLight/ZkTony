package com.zktony.serialport.command

import com.zktony.serialport.ext.crc16LE
import com.zktony.serialport.ext.replaceByteArrayBE
import com.zktony.serialport.ext.writeInt16LE

/**
 * Protocol
 *
 * @property head Byte
 * @property addr Byte
 * @property func Byte
 * @property length ByteArray
 * @property data ByteArray
 * @property crc ByteArray
 * @property end ByteArray
 */
class Protocol {
    // Head 1byte 0xEE
    var head: Byte = 0xEE.toByte()

    // Addr 1byte 0x01 0x02
    var addr: Byte = 0x01.toByte()

    /**
     * control
     *
     * TX: 0x00 reset
     *     0x01 move
     *     0x02 stop
     *     0x03 query axis
     *     0x04 query gpio
     *     0x05 valve
     *
     * RX: 0x01 motor status
     *     0x02 gpio status
     *     0xFF error
     */
    var func: Byte = 0x01.toByte()

    // Length of data 2byte 0x0000 ~ 0xFFFF
    var length: ByteArray = byteArrayOf(0x00.toByte(), 0x00.toByte())

    // Data 0 ~ 255 byte
    var data: ByteArray = byteArrayOf()

    // Crc16 modbus 2byte 0x0000 ~ 0xFFFF
    var crc: ByteArray = byteArrayOf(0x00.toByte(), 0x00.toByte())

    // End 4byte 0xFF 0xFC 0xFF 0xFF
    var end: ByteArray = byteArrayOf(0xFF.toByte(), 0xFC.toByte(), 0xFF.toByte(), 0xFF.toByte())

    fun toByteArray(): ByteArray {
        val byteArray = byteArrayOf(head, addr, func)
            .plus(length.writeInt16LE(data.size, 0))
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

/**
 * protocol
 *
 * @param block [@kotlin.ExtensionFunctionType] Function1<Protocol, Unit>
 * @return Protocol
 */
fun protocol(block: Protocol.() -> Unit): Protocol {
    return Protocol().apply(block)
}

/**
 * protocol
 *
 * @receiver ByteArray
 * @return Protocol
 */
fun ByteArray.protocol(): Protocol {
    val bytes = this
    return protocol {
        head = bytes[0]
        addr = bytes[1]
        func = bytes[2]
        length = bytes.copyOfRange(3, 5)
        data = bytes.copyOfRange(5, bytes.size - 6)
        crc = bytes.copyOfRange(bytes.size - 6, bytes.size - 4)
        end = bytes.copyOfRange(bytes.size - 4, bytes.size)
    }
}