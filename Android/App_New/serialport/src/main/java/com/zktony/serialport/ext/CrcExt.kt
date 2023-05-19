package com.zktony.serialport.ext


/**
 * CRC16_CCITT：多项式x16+x12+x5+1（0x1021），初始值0x0000，低位在前，高位在后，结果与0x0000异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16ccitt(): ByteArray {
    var crc = 0x0000
    for (j in this) {
        crc = crc xor (j.toInt() and 0xff)
        for (i in 0..7) {
            crc = if (crc and 1 != 0) crc shr 1 xor 0x8408 else crc shr 1 // 0x8408 = reverse 0x1021
        }
    }
    return byteArrayOf((crc shr 8 and 0xff).toByte(), (crc and 0xff).toByte())
}

/**
 * CRC16_CCITT_FALSE：多项式x16+x12+x5+1（0x1021），初始值0xFFFF，低位在后，高位在前，结果与0x0000异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16ccittFalse(): ByteArray {
    var crc = 0xffff
    for (j in this) {
        crc = crc xor (j.toInt() shl 8)
        for (i in 0..7) {
            crc =
                if (crc and 0x8000 != 0) crc shl 1 xor 0x1021 else crc shl 1 // 0x1021 = reverse 0x8408
        }
    }
    return byteArrayOf((crc shr 8 and 0xff).toByte(), (crc and 0xff).toByte())
}

/**
 * CRC16_XMODEM：多项式x16+x12+x5+1（0x1021），初始值0x0000，低位在后，高位在前，结果与0x0000异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16xmodem(): ByteArray {
    var crc = 0x0000
    for (j in this) {
        crc = crc xor (j.toInt() shl 8)
        for (i in 0..7) {
            crc =
                if (crc and 0x8000 != 0) crc shl 1 xor 0x1021 else crc shl 1 // 0x1021 = reverse 0x8408
        }
    }
    return byteArrayOf((crc shr 8 and 0xff).toByte(), (crc and 0xff).toByte())
}

/**
 * CRC16_X25：多项式x16+x12+x5+1（0x1021），初始值0xFFFF，低位在前，高位在后，结果与0xFFFF异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16x25(): ByteArray {
    var crc = 0xffff
    for (j in this) {
        crc = crc xor j.toInt()
        for (i in 0..7) {
            crc = if (crc and 1 != 0) crc shr 1 xor 0x8408 else crc shr 1 // 0x8408 = reverse 0x1021
        }
    }
    crc = crc.inv() // crc^0xffff
    return byteArrayOf((crc shr 8 and 0xff).toByte(), (crc and 0xff).toByte())
}

/**
 * CRC16_MODBUS：多项式x16+x15+x2+1（0x8005），初始值0xFFFF，低位在前，高位在后，结果与0x0000异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16modbus(): ByteArray {
    var crc = 0xffff
    for (j in this) {
        crc = crc xor (j.toInt() and 0xff) and 0xffff
        for (i in 0..7) {
            crc =
                if (crc and 1 == 0) crc shr 1 else crc shr 1 xor 0xA001 and 0xffff // 0xA001 = reverse 0x8005
        }
    }
    return byteArrayOf((crc shr 8 and 0xff).toByte(), (crc and 0xff).toByte())
}

/**
 * CRC16_IBM：多项式x16+x15+x2+1（0x8005），初始值0x0000，低位在前，高位在后，结果与0x0000异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16ibm(): ByteArray {
    var crc = 0x0000
    for (j in this) {
        crc = crc xor j.toInt()
        for (i in 0..7) {
            crc =
                if (crc and 1 == 0) crc shr 1 else crc shr 1 xor 0xA001  // 0xA001 = reverse 0x8005
        }
    }
    return byteArrayOf((crc shr 8 and 0xff).toByte(), (crc and 0xff).toByte())
}

/**
 * CRC16_MAXIM：多项式x16+x15+x2+1（0x8005），初始值0x0000，低位在前，高位在后，结果与0xFFFF异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16maxim(): ByteArray {
    var crc = 0x0000
    for (j in this) {
        crc = crc xor j.toInt()
        for (i in 0..7) {
            crc =
                if (crc and 1 == 0) crc shr 1 else crc shr 1 xor 0xA001  // 0xA001 = reverse 0x8005
        }
    }
    crc = crc.inv() // crc^0xffff
    return byteArrayOf((crc shr 8 and 0xff).toByte(), (crc and 0xff).toByte())
}

/**
 * CRC16_USB：多项式x16+x15+x2+1（0x8005），初始值0xFFFF，低位在前，高位在后，结果与0xFFFF异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16usb(): ByteArray {
    var crc = 0xffff
    for (j in this) {
        crc = crc xor j.toInt()
        for (i in 0..7) {
            crc =
                if (crc and 1 == 0) crc shr 1 else crc shr 1 xor 0xA001  // 0xA001 = reverse 0x8005
        }
    }
    crc = crc.inv() // crc^0xffff
    return byteArrayOf((crc shr 8 and 0xff).toByte(), (crc and 0xff).toByte())
}

/**
 * CRC16_DNP：多项式x16+x13+x12+x11+x10+x8+x6+x5+x2+1（0x3D65），初始值0x0000，低位在前，高位在后，结果与0xFFFF异或
 *
 * @receiver ByteArray
 * @return ByteArray
 */
fun ByteArray.crc16dnp(): ByteArray {
    var crc = 0x0000
    for (j in this) {
        crc = crc xor j.toInt()
        for (i in 0..7) {
            crc =
                if (crc and 1 != 0) crc shr 1 xor 0xA6BC else crc shr 1 // 0xA6BC = reverse 0x3D65
        }
    }
    crc = crc.inv()
    return byteArrayOf((crc shr 8 and 0xff).toByte(), (crc and 0xff).toByte())
}

/**
 * crc16
 *
 * @receiver ByteArray
 * @param method String
 * @return ByteArray
 */
fun ByteArray.crc16(method: String = "MODBUS"): ByteArray {
    return when (method) {
        "CCITT" -> crc16ccitt()
        "CCITT_FALSE" -> crc16ccittFalse()
        "XMODEM" -> crc16xmodem()
        "X25" -> crc16x25()
        "MODBUS" -> crc16modbus()
        "IBM" -> crc16ibm()
        "MAXIM" -> crc16maxim()
        "USB" -> crc16usb()
        "DNP" -> crc16dnp()
        else -> crc16modbus()
    }
}