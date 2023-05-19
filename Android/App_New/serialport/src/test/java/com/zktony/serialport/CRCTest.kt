package com.zktony.serialport

import com.zktony.serialport.ext.crc16
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
        assertEquals("D8 41", bytes.crc16("CCITT").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("E3 1D", bytes1.crc16("CCITT").toHexString())
    }

    @Test
    fun crc16ccittFalse() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("3F BD", bytes.crc16("CCITT_FALSE").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("EE 9E", bytes1.crc16("CCITT_FALSE").toHexString())
    }

    @Test
    fun crc16xmodem() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("7E 55", bytes.crc16("XMODEM").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("E0 8E", bytes1.crc16("XMODEM").toHexString())
    }

    @Test
    fun crc16x25() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("60 CF", bytes.crc16("X25").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("14 92", bytes1.crc16("X25").toHexString())
    }

    @Test
    fun crc16modbus() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("DE 6C", bytes.crc16("MODBUS").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("12 8A", bytes1.crc16("MODBUS").toHexString())
    }

    @Test
    fun crc16ibm() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("11 F9", bytes.crc16("IBM").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("09 8A", bytes1.crc16("IBM").toHexString())
    }

    @Test
    fun crc16maxim() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("EE 06", bytes.crc16("MAXIM").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("F6 75", bytes1.crc16("MAXIM").toHexString())
    }

    @Test
    fun crc16usb() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("8A B9", bytes.crc16("USB").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("ED 75", bytes1.crc16("USB").toHexString())
    }

    @Test
    fun crc16dnp() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("DD 27", bytes.crc16("DNP").toHexString())
        val bytes1 = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        assertEquals("01060A000000", bytes1.toHexString(false))
        assertEquals("BD 8A", bytes1.crc16("DNP").toHexString())
    }

}