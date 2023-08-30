package com.zktony.serialport.command.modbus

import com.zktony.serialport.ext.crc16LE

/**
 * Modbus RTU 协议
 */

class RtuProtocol {
    var slaveAddr: Byte = 0x01
    var funcCode: Byte = 0x03
    var data: ByteArray = byteArrayOf()
    var crc: ByteArray = byteArrayOf(0x00.toByte(), 0x00.toByte())

    fun toByteArray(): ByteArray {
        val byteArray = byteArrayOf(slaveAddr, funcCode).plus(data)
        return byteArray.plus(byteArray.crc16LE())
    }
}

fun rtuProtocol(block: RtuProtocol.() -> Unit): RtuProtocol {
    return RtuProtocol().apply(block)
}

fun ByteArray.toRtuProtocol(): RtuProtocol {
    val byteArray = this
    return rtuProtocol {
        slaveAddr = byteArray[0]
        funcCode = byteArray[1]
        data = byteArray.copyOfRange(2, byteArray.size - 2)
        crc = byteArray.copyOfRange(byteArray.size - 2, byteArray.size)
    }
}