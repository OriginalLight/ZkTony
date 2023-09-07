package com.zktony.serialport.command

import com.zktony.serialport.ext.*

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
class Protocol : BaseProtocol<Protocol> {
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

    override fun toByteArray(): ByteArray {
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

    override fun toProtocol(byteArray: ByteArray): Protocol {
        head = byteArray[0]
        addr = byteArray[1]
        func = byteArray[2]
        length = byteArray.copyOfRange(3, 5)
        data = byteArray.copyOfRange(5, byteArray.size - 6)
        crc = byteArray.copyOfRange(byteArray.size - 6, byteArray.size - 4)
        end = byteArray.copyOfRange(byteArray.size - 4, byteArray.size)
        return this
    }

    override fun callbackHandler(byteArray: ByteArray, block: (Int, Protocol) -> Unit) {
        // 验证包长 >= 12
        if (byteArray.size < 12) {
            throw Exception("RX Length Error")
        }

        // 分包处理
        val expectHead = byteArrayOf(0xEE.toByte())
        val expectEnd = byteArrayOf(0xFF.toByte(), 0xFC.toByte(), 0xFF.toByte(), 0xFF.toByte())
        byteArray.splitByteArray(expectHead, expectEnd).forEach { packet ->
            // 验证包头和包尾
            val head = packet.copyOfRange(0, 1)
            if (!head.contentEquals(expectHead)) {
                throw Exception("RX Header Error")
            }
            val end = packet.copyOfRange(packet.size - 4, packet.size)
            if (!end.contentEquals(expectEnd)) {
                throw Exception("RX End Error")
            }

            // crc 校验
            val crc = packet.copyOfRange(packet.size - 6, packet.size - 4)
            val bytes = packet.copyOfRange(0, packet.size - 6)
            if (!bytes.crc16LE().contentEquals(crc)) {
                throw Exception("RX Crc Error")
            }

            // 校验通过
            // 解析协议
            val rx = toProtocol(packet)

            // 处理地址为 0x02 的数据包
            if (rx.addr == 0x02.toByte()) {
                when (rx.func) {
                    // 处理轴状态数据
                    0x01.toByte() -> {
                        block(AXIS, rx)
                    }
                    // 处理 GPIO 状态数据
                    0x02.toByte() -> {
                        block(GPIO, rx)
                    }
                    // 处理错误信息
                    0xFF.toByte() -> {
                        when (rx.data.readInt16LE()) {
                            1 -> throw Exception("TX Header Error")
                            2 -> throw Exception("TX Addr Error")
                            3 -> throw Exception("TX Crc Error")
                            4 -> throw Exception("TX No Com")
                        }
                    }
                }
            }

        }
    }

    companion object {
        const val AXIS = 0
        const val GPIO = 1
    }
}