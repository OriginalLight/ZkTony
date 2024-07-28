package com.zktony.serialport.protocol

import com.zktony.serialport.ext.crc16LE
import com.zktony.serialport.ext.toHexString

/**
 * Modbus RTU 协议
 */

data class RtuProtocol(
    var target: Byte = 0x01,
    var func: Byte = 0x03,
    var data: ByteArray = byteArrayOf(),
    val crc: ByteArray = byteArrayOf(0x00.toByte(), 0x00.toByte())
) {
    // 生成协议
    fun toByteArray(): ByteArray {
        val byteArray = byteArrayOf(target, func).plus(data)
        return byteArray.plus(byteArray.crc16LE())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RtuProtocol

        if (target != other.target) return false
        if (func != other.func) return false
        if (!data.contentEquals(other.data)) return false
        if (!crc.contentEquals(other.crc)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = target.hashCode()
        result = 31 * result + func.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + crc.contentHashCode()
        return result
    }


    companion object {

        /**
         * 解析协议
         * @param byteArray ByteArray
         * @return RtuProtocol
         */
        fun toObject(byteArray: ByteArray): RtuProtocol {
            try {
                return RtuProtocol(
                    target = byteArray[0],
                    func = byteArray[1],
                    data = byteArray.copyOfRange(2, byteArray.size - 2),
                    crc = byteArray.copyOfRange(byteArray.size - 2, byteArray.size)
                )
            } catch (e: Exception) {
                throw Exception("to RtuProtocol error by ${byteArray.toHexString()}")
            }
        }

        /**
         * Verify the protocol
         * @param byteArray ByteArray
         * @param block Function1<[@kotlin.ParameterName] RtuProtocol, Unit>
         * @throws Exception
         */
        @kotlin.jvm.Throws(Exception::class)
        fun verifyProtocol(byteArray: ByteArray, block: (RtuProtocol) -> Unit) {
            // crc 校验
            val crc = byteArray.copyOfRange(byteArray.size - 2, byteArray.size)
            val bytes = byteArray.copyOfRange(0, byteArray.size - 2)
            if (!bytes.crc16LE().contentEquals(crc)) {
                throw Exception("rx crc error by ${byteArray.toHexString()}")
            }

            block(toObject(byteArray))
        }
    }
}