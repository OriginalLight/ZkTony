package com.zktony.serialport.command.modbus

import com.zktony.serialport.command.BaseProtocol
import com.zktony.serialport.ext.crc16LE
import com.zktony.serialport.ext.toHexString

/**
 * Modbus RTU 协议
 */

class RtuProtocol : BaseProtocol<RtuProtocol> {
    var slaveAddr: Byte = 0x01
    var funcCode: Byte = 0x03
    var data: ByteArray = byteArrayOf()
    var crc: ByteArray = byteArrayOf(0x00.toByte(), 0x00.toByte())

    override fun toByteArray(): ByteArray {
        val byteArray = byteArrayOf(slaveAddr, funcCode).plus(data)
        return byteArray.plus(byteArray.crc16LE())
    }

    override fun toProtocol(byteArray: ByteArray): RtuProtocol {
        slaveAddr = byteArray[0]
        funcCode = byteArray[1]
        data = byteArray.copyOfRange(2, byteArray.size - 2)
        crc = byteArray.copyOfRange(byteArray.size - 2, byteArray.size)
        return this
    }

    override fun callbackHandler(byteArray: ByteArray, block: (Int, RtuProtocol) -> Unit) {
        // crc 校验
        val crc = byteArray.copyOfRange(byteArray.size - 2, byteArray.size)
        val bytes = byteArray.copyOfRange(0, byteArray.size - 2)
        if (!bytes.crc16LE().contentEquals(crc)) {
            throw Exception("RX Crc Error with byteArray: ${byteArray.toHexString()}")
        }
        // 解析协议
        val rx = toProtocol(byteArray)
        when (rx.funcCode) {
            0x03.toByte() -> {
                block(LOCATION, rx)
            }

            else -> {}
        }
    }

    companion object {
        const val LOCATION = 0
    }
}