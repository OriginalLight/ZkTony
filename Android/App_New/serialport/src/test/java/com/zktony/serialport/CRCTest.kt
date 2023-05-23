package com.zktony.serialport

import com.zktony.serialport.ext.crc16BE
import com.zktony.serialport.ext.hex2ByteArray
import com.zktony.serialport.ext.toHexString
import org.junit.Assert.assertEquals
import org.junit.Test

class CRCTest {
    @Test
    fun crc16ccitt() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("D8 41", bytes.crc16BE("CCITT").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("E3 1D", bytes1.crc16BE("CCITT").toHexString())
    }

    @Test
    fun crc16ccittFalse() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("3F BD", bytes.crc16BE("CCITT_FALSE").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("EE 9E", bytes1.crc16BE("CCITT_FALSE").toHexString())
    }

    @Test
    fun crc16xmodem() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("7E 55", bytes.crc16BE("XMODEM").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("E0 8E", bytes1.crc16BE("XMODEM").toHexString())
    }

    @Test
    fun crc16x25() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("60 CF", bytes.crc16BE("X25").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("14 92", bytes1.crc16BE("X25").toHexString())
    }

    @Test
    fun crc16modbus() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("DE 6C", bytes.crc16BE("MODBUS").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("12 8A", bytes1.crc16BE("MODBUS").toHexString())
    }

    @Test
    fun crc16ibm() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("11 F9", bytes.crc16BE("IBM").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("09 8A", bytes1.crc16BE("IBM").toHexString())
    }

    @Test
    fun crc16maxim() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("EE 06", bytes.crc16BE("MAXIM").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("F6 75", bytes1.crc16BE("MAXIM").toHexString())
    }

    @Test
    fun crc16usb() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("8A B9", bytes.crc16BE("USB").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("ED 75", bytes1.crc16BE("USB").toHexString())
    }

    @Test
    fun crc16dnp() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("DD 27", bytes.crc16BE("DNP").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("BD 8A", bytes1.crc16BE("DNP").toHexString())
    }

    @Test
    fun test2() {
        val str = "EE 01 01 0B 00 01 00 01 86 A0 0C 80 0C 80 19 00"
        val bytes = str.hex2ByteArray()
        val crc = bytes.crc16BE("MODBUS")
        assertEquals("40 81", crc.toHexString())
    }

    @Test
    fun le() {
        val i = 1
        val low = i and 0xFF
        val high = i shr 8 and 0xFF
        assertEquals(1, low)
        assertEquals(0, high)
    }

}