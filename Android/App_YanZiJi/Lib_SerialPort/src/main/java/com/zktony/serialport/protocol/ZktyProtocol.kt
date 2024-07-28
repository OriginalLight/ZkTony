package com.zktony.serialport.protocol

import com.zktony.serialport.ext.crc16LE
import com.zktony.serialport.ext.replaceByteArrayBE
import com.zktony.serialport.ext.toHexString
import com.zktony.serialport.ext.writeInt16LE

data class ZktyProtocol(
    val header: Byte = 0xEE.toByte(),
    var source: Byte = 0x01.toByte(),
    var target: Byte = 0x01.toByte(),
    var func: Byte = 0x01.toByte(),
    val len: ByteArray = byteArrayOf(0x00.toByte(), 0x00.toByte()),
    var data: ByteArray = byteArrayOf(),
    val crc: ByteArray = byteArrayOf(0x00.toByte(), 0x00.toByte()),
    val end: ByteArray = byteArrayOf(0xFC.toByte(), 0xFF.toByte())
) {
    // 生成协议
    fun toByteArray(): ByteArray {
        val byteArray = byteArrayOf(header, source, target, func)
            .plus(len.writeInt16LE(data.size))
            .plus(data)
            .plus(crc)
            .plus(end)

        // crc
        return byteArray.replaceByteArrayBE(
            byteArray.copyOfRange(0, byteArray.size - 4).crc16LE(),
            byteArray.size - 4,
            0
        )
    }
    // 重写equals
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ZktyProtocol

        if (header != other.header) return false
        if (source != other.source) return false
        if (target != other.target) return false
        if (func != other.func) return false
        if (!len.contentEquals(other.len)) return false
        if (!data.contentEquals(other.data)) return false
        if (!crc.contentEquals(other.crc)) return false
        if (!end.contentEquals(other.end)) return false

        return true
    }
    // 重写hashCode
    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + source.hashCode()
        result = 31 * result + target.hashCode()
        result = 31 * result + func.hashCode()
        result = 31 * result + len.contentHashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + crc.contentHashCode()
        result = 31 * result + end.contentHashCode()
        return result
    }

    companion object {
        // 协议包头和包尾
        private val expectHeader = byteArrayOf(0xEE.toByte())
        private val expectEnd = byteArrayOf(0xFC.toByte(), 0xFF.toByte())

        /**
         * Convert ByteArray to Protocol
         */
        fun toObject(byteArray: ByteArray): ZktyProtocol {
            try {
                return ZktyProtocol(
                    header = byteArray[0],
                    source = byteArray[1],
                    target = byteArray[2],
                    func = byteArray[3],
                    len = byteArray.copyOfRange(4, 6),
                    data = byteArray.copyOfRange(6, byteArray.size - 6),
                    crc = byteArray.copyOfRange(byteArray.size - 6, byteArray.size - 4),
                    end = byteArray.copyOfRange(byteArray.size - 4, byteArray.size)
                )
            } catch (e: Exception) {
                throw Exception("to Protocol error by ${byteArray.toHexString()}")
            }
        }

        /**
         * Verify the protocol
         * @param byteArray ByteArray
         * @param block Function1<[@kotlin.ParameterName] Protocol, Unit>
         * @throws Exception
         */
        @kotlin.jvm.Throws(Exception::class)
        fun verifyProtocol(byteArray: ByteArray, block: (ZktyProtocol) -> Unit) {
            // 验证包长 >= 10
            if (byteArray.size < 10) {
                throw Exception("rx length error by ${byteArray.toHexString()}")
            }

            // 验证包头
            val head = byteArray.copyOfRange(0, 1)
            if (!head.contentEquals(expectHeader)) {
                throw Exception("rx header error by ${byteArray.toHexString()}")
            }

            // 验证包尾
            val end = byteArray.copyOfRange(byteArray.size - 2, byteArray.size)
            if (!end.contentEquals(expectEnd)) {
                throw Exception("rx end error by ${byteArray.toHexString()}")
            }

            // crc 校验
            val crc = byteArray.copyOfRange(byteArray.size - 4, byteArray.size - 2)
            val bytes = byteArray.copyOfRange(0, byteArray.size - 4)
            if (!bytes.crc16LE().contentEquals(crc)) {
                throw Exception("rx crc error by ${byteArray.toHexString()}")
            }

            // 解析协议
            block(toObject(byteArray))
        }
    }
}