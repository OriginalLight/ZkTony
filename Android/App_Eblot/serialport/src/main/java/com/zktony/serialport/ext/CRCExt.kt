package com.zktony.serialport.ext

const val BITS_OF_BYTE = 8
const val POLYNOMIAL = 0xA001
const val INITIAL_VALUE = 0xFFFF
const val FF = 0xFF

/**
 * CRC16_MODBUS：多项式x16+x15+x2+1（0x8005），初始值0xFFFF，低位在前，高位在后，结果与0x0000异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16(): ByteArray {
    var res = INITIAL_VALUE
    for (data in this) {
        res = res xor (data.toInt() and FF)
        for (i in 0 until BITS_OF_BYTE) {
            res = if (res and 0x0001 == 1) res shr 1 xor POLYNOMIAL else res shr 1
        }
    }
    val lowByte: Byte = (res shr 8 and FF).toByte()
    val highByte: Byte = (res and FF).toByte()
    return byteArrayOf(lowByte, highByte)
}

/**
 * String to CRC16
 *
 * @receiver String
 * @return String
 */
fun String.crc16(): String {
    val hex = this.trim { it <= ' ' }.replace(" ", "")
    val bOutArray = hex.hexStringToByteArray()
    val crc16 = bOutArray.crc16()
    return crc16.byteArrayToHexString()
}