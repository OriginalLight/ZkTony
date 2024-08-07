package com.zktony.serialport.command

import com.zktony.serialport.ext.crc16LE
import com.zktony.serialport.ext.replaceByteArrayBE
import com.zktony.serialport.ext.splitByteArray
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
class Protocol : BaseProtocol {
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

    override fun serialization(): ByteArray {
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

    override fun deserialization(byteArray: ByteArray) {
        head = byteArray[0]
        addr = byteArray[1]
        func = byteArray[2]
        length = byteArray.copyOfRange(3, 5)
        data = byteArray.copyOfRange(5, byteArray.size - 6)
        crc = byteArray.copyOfRange(byteArray.size - 6, byteArray.size - 4)
        end = byteArray.copyOfRange(byteArray.size - 4, byteArray.size)
    }

    companion object {
        // 协议包头和包尾
        private val expectHead = byteArrayOf(0xEE.toByte())
        private val expectEnd =
            byteArrayOf(0xFF.toByte(), 0xFC.toByte(), 0xFF.toByte(), 0xFF.toByte())

        /**
         * Verify the protocol
         * @param byteArray ByteArray
         * @param block Function1<[@kotlin.ParameterName] Protocol, Unit>
         * @throws Exception
         */
        @kotlin.jvm.Throws(Exception::class)
        fun verifyProtocol(byteArray: ByteArray, block: (Protocol) -> Unit) {
            // 验证包长 >= 11
            if (byteArray.size < 11) {
                throw Exception("RX Length Error")
            }

            // 分包处理
            byteArray.splitByteArray(expectHead, expectEnd).forEach { pkg ->
                // 验证包头和包尾
                val head = pkg.copyOfRange(0, 1)
                if (!head.contentEquals(expectHead)) {
                    throw Exception("RX Header Error")
                }
                val end = pkg.copyOfRange(pkg.size - 4, pkg.size)
                if (!end.contentEquals(expectEnd)) {
                    throw Exception("RX End Error")
                }

                // crc 校验
                val crc = pkg.copyOfRange(pkg.size - 6, pkg.size - 4)
                val bytes = pkg.copyOfRange(0, pkg.size - 6)
                if (!bytes.crc16LE().contentEquals(crc)) {
                    throw Exception("RX Crc Error")
                }

                // 解析协议
                block(Protocol().apply { deserialization(pkg) })
            }
        }
    }
}