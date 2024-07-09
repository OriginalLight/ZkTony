package com.zktony.serialport.command

import com.zktony.serialport.ext.crc16LE
import com.zktony.serialport.ext.replaceByteArrayBE
import com.zktony.serialport.ext.splitByteArray
import com.zktony.serialport.ext.toHexString
import com.zktony.serialport.ext.writeInt16LE

/**
 * Protocol
 *
 * @property frameHeader Byte
 * @property targetAddress Byte
 * @property function Byte
 * @property length ByteArray
 * @property data ByteArray
 * @property crc ByteArray
 * @property frameEnd ByteArray
 */
class Protocol : BaseProtocol {
    // 帧头 1byte 0xEE
    private var frameHeader: Byte = 0xEE.toByte()
    // 源地址 1byte 0x01
    var sourceAddress: Byte = 0x01.toByte()
    // 目标地址 1byte 0x01 0x02 0x03 0x04 0x05 0x06 0x07 0x08
    var targetAddress: Byte = 0x01.toByte()

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
    var function: Byte = 0x01.toByte()

    // Length of data 2byte 0x0000 ~ 0xFFFF
    var length: ByteArray = byteArrayOf(0x00.toByte(), 0x00.toByte())

    // Data 0 ~ 255 byte
    var data: ByteArray = byteArrayOf()

    // Crc16 modbus 2byte 0x0000 ~ 0xFFFF
    var crc: ByteArray = byteArrayOf(0x00.toByte(), 0x00.toByte())

    // End 4byte 0xFF 0xFC 0xFF 0xFF
    private var frameEnd: ByteArray = byteArrayOf(0xFF.toByte(), 0xFC.toByte())

    override fun serialization(): ByteArray {
        val byteArray = byteArrayOf(frameHeader, sourceAddress, targetAddress, function)
            .plus(length.writeInt16LE(data.size, 0))
            .plus(data)
            .plus(crc)
            .plus(frameEnd)

        // crc
        return byteArray.replaceByteArrayBE(
            byteArray.copyOfRange(0, byteArray.size - 4).crc16LE(),
            byteArray.size - 4,
            0
        )
    }

    override fun deserialization(byteArray: ByteArray) {
        frameHeader = byteArray[0]
        sourceAddress = byteArray[1]
        targetAddress = byteArray[2]
        function = byteArray[3]
        length = byteArray.copyOfRange(4, 6)
        data = byteArray.copyOfRange(6, byteArray.size - 4)
        crc = byteArray.copyOfRange(byteArray.size - 4, byteArray.size - 2)
        frameEnd = byteArray.copyOfRange(byteArray.size - 2, byteArray.size)
    }

    companion object {
        // 协议包头和包尾
        private val expectHead = byteArrayOf(0xEE.toByte())
        private val expectEnd = byteArrayOf(0xFF.toByte(), 0xFC.toByte())

        /**
         * Verify the protocol
         * @param byteArray ByteArray
         * @param block Function1<[@kotlin.ParameterName] Protocol, Unit>
         * @throws Exception
         */
        @kotlin.jvm.Throws(Exception::class)
        fun verifyProtocol(byteArray: ByteArray, block: (Protocol) -> Unit) {
            // 验证包长 >= 10
            if (byteArray.size < 10) {
                throw Exception("Rx length error by ${byteArray.toHexString()}")
            }

            // 分包处理
            byteArray.splitByteArray().forEach { pkg ->
                // 验证包头和包尾
                val head = pkg.copyOfRange(0, 1)
                if (!head.contentEquals(expectHead)) {
                    throw Exception("Rx header error by ${pkg.toHexString()}")
                }
                val end = pkg.copyOfRange(pkg.size - 2, pkg.size)
                if (!end.contentEquals(expectEnd)) {
                    throw Exception("Rx end error by ${pkg.toHexString()}")
                }

                // crc 校验
                val crc = pkg.copyOfRange(pkg.size - 4, pkg.size - 2)
                val bytes = pkg.copyOfRange(0, pkg.size - 4)
                if (!bytes.crc16LE().contentEquals(crc)) {
                    throw Exception("Rx crc error by ${pkg.toHexString()}")
                }

                // 解析协议
                block(Protocol().apply { deserialization(pkg) })
            }
        }
    }
}