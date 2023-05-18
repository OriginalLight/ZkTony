package com.zktony.serialport

import com.zktony.serialport.ext.crc16
import com.zktony.serialport.ext.toHexString
import org.junit.Assert.assertEquals
import org.junit.Test

class CRCTest {

    @Test
    fun crc1() {
        val bytes = ByteArray(256)
        for (i in 0..255) {
            bytes[i] = i.toByte()
        }
        assertEquals("DE 6C", bytes.crc16().toHexString())
    }

    @Test
    fun crc2() {
        val bytes = byteArrayOf(
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )
        val crc16 = bytes.crc16()
        assertEquals(0x12.toByte(), crc16[0])
        assertEquals(0x8A.toByte(), crc16[1])
    }

}