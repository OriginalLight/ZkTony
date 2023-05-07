package com.zktony.serialport

import com.zktony.serialport.ext.crc16
import com.zktony.serialport.ext.byteToHex
import org.junit.Assert.assertEquals
import org.junit.Test

class CRCTest {

    @Test
    fun crc1() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        val crc16 = bytes.crc16()
        assertEquals("DE", crc16[0].byteToHex())
        assertEquals("6C", crc16[1].byteToHex())
    }

    @Test
    fun crc2() {
        val bytes = byteArrayOf(0x01.toByte(), 0x06.toByte(), 0x0A.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())
        val crc16 = bytes.crc16()
        assertEquals("12", crc16[0].byteToHex())
        assertEquals("8A", crc16[1].byteToHex())
    }


    @Test
    fun crc3() {
        val text = "01060A000000"
        val crc16 = text.crc16()
        assertEquals("128A", crc16)
    }
}