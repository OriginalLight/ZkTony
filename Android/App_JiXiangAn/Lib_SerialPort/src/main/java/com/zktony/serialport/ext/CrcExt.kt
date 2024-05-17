package com.zktony.serialport.ext


/**
 * CRC16_CCITT：多项式x16+x12+x5+1（0x1021），初始值0x0000，低位在前，高位在后，结果与0x0000异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16ccitt(): Int {
    var crc = 0x0000
    for (j in this) {
        crc = crc xor (j.toInt() and 0xff)
        for (i in 0..7) {
            crc = if (crc and 1 != 0) crc shr 1 xor 0x8408 else crc shr 1 // 0x8408 = reverse 0x1021
        }
    }
    return crc
}

/**
 * CRC16_CCITT_FALSE：多项式x16+x12+x5+1（0x1021），初始值0xFFFF，低位在后，高位在前，结果与0x0000异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16ccittFalse(): Int {
    var crc = 0xffff
    for (j in this) {
        crc = crc xor (j.toInt() shl 8)
        for (i in 0..7) {
            crc =
                if (crc and 0x8000 != 0) crc shl 1 xor 0x1021 else crc shl 1 // 0x1021 = reverse 0x8408
        }
    }
    return crc
}

/**
 * CRC16_XMODEM：多项式x16+x12+x5+1（0x1021），初始值0x0000，低位在后，高位在前，结果与0x0000异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16xmodem(): Int {
    var crc = 0x0000
    for (j in this) {
        crc = crc xor (j.toInt() shl 8)
        for (i in 0..7) {
            crc =
                if (crc and 0x8000 != 0) crc shl 1 xor 0x1021 else crc shl 1 // 0x1021 = reverse 0x8408
        }
    }
    return crc
}

/**
 * CRC16_X25：多项式x16+x12+x5+1（0x1021），初始值0xFFFF，低位在前，高位在后，结果与0xFFFF异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16x25(): Int {
    var crc = 0xffff
    for (j in this) {
        crc = crc xor j.toInt()
        for (i in 0..7) {
            crc = if (crc and 1 != 0) crc shr 1 xor 0x8408 else crc shr 1 // 0x8408 = reverse 0x1021
        }
    }
    return crc.inv() // crc^0xffff
}

/**
 * CRC16_MODBUS：多项式x16+x15+x2+1（0x8005），初始值0xFFFF，低位在前，高位在后，结果与0x0000异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16modbus(): Int {
    var crc = 0xffff
    for (j in this) {
        crc = crc xor (j.toInt() and 0xff) and 0xffff
        for (i in 0..7) {
            crc =
                if (crc and 1 == 0) crc shr 1 else crc shr 1 xor 0xA001 and 0xffff // 0xA001 = reverse 0x8005
        }
    }
    return crc
}

/**
 * CRC16_IBM：多项式x16+x15+x2+1（0x8005），初始值0x0000，低位在前，高位在后，结果与0x0000异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16ibm(): Int {
    var crc = 0x0000
    for (j in this) {
        crc = crc xor j.toInt()
        for (i in 0..7) {
            crc =
                if (crc and 1 == 0) crc shr 1 else crc shr 1 xor 0xA001  // 0xA001 = reverse 0x8005
        }
    }
    return crc
}

/**
 * CRC16_MAXIM：多项式x16+x15+x2+1（0x8005），初始值0x0000，低位在前，高位在后，结果与0xFFFF异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16maxim(): Int {
    var crc = 0x0000
    for (j in this) {
        crc = crc xor j.toInt()
        for (i in 0..7) {
            crc =
                if (crc and 1 == 0) crc shr 1 else crc shr 1 xor 0xA001  // 0xA001 = reverse 0x8005
        }
    }
    return crc.inv() // crc^0xffff
}

/**
 * CRC16_USB：多项式x16+x15+x2+1（0x8005），初始值0xFFFF，低位在前，高位在后，结果与0xFFFF异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16usb(): Int {
    var crc = 0xffff
    for (j in this) {
        crc = crc xor j.toInt()
        for (i in 0..7) {
            crc =
                if (crc and 1 == 0) crc shr 1 else crc shr 1 xor 0xA001  // 0xA001 = reverse 0x8005
        }
    }
    return crc.inv() // crc^0xffff
}

/**
 * CRC16_DNP：多项式x16+x13+x12+x11+x10+x8+x6+x5+x2+1（0x3D65），初始值0x0000，低位在前，高位在后，结果与0xFFFF异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16dnp(): Int {
    var crc = 0x0000
    for (j in this) {
        crc = crc xor j.toInt()
        for (i in 0..7) {
            crc =
                if (crc and 1 != 0) crc shr 1 xor 0xA6BC else crc shr 1 // 0xA6BC = reverse 0x3D65
        }
    }
    return crc.inv()
}

/**
 * crc16LE
 *
 * @receiver ByteArray
 * @param type CrcEnum
 * @return ByteArray
 */
fun ByteArray.crc16LE(type: CrcType = CrcType.MODBUS): ByteArray {
    val crc = when (type) {
        CrcType.CCITT -> crc16ccitt()
        CrcType.CCITT_FALSE -> crc16ccittFalse()
        CrcType.XMODEM -> crc16xmodem()
        CrcType.X25 -> crc16x25()
        CrcType.MODBUS -> crc16modbus()
        CrcType.IBM -> crc16ibm()
        CrcType.MAXIM -> crc16maxim()
        CrcType.USB -> crc16usb()
        CrcType.DNP -> crc16dnp()
    }
    return byteArrayOf((crc and 0xff).toByte(), (crc shr 8 and 0xff).toByte())
}

/**
 * crc16BE
 *
 * @receiver ByteArray
 * @param type CrcEnum
 * @return ByteArray
 */
fun ByteArray.crc16BE(type: CrcType = CrcType.MODBUS): ByteArray {
    val crc = when (type) {
        CrcType.CCITT -> crc16ccitt()
        CrcType.CCITT_FALSE -> crc16ccittFalse()
        CrcType.XMODEM -> crc16xmodem()
        CrcType.X25 -> crc16x25()
        CrcType.MODBUS -> crc16modbus()
        CrcType.IBM -> crc16ibm()
        CrcType.MAXIM -> crc16maxim()
        CrcType.USB -> crc16usb()
        CrcType.DNP -> crc16dnp()
    }
    return byteArrayOf((crc shr 8 and 0xff).toByte(), (crc and 0xff).toByte())
}

enum class CrcType {
    CCITT, CCITT_FALSE, XMODEM, X25, MODBUS, IBM, MAXIM, USB, DNP
}