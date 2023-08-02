package com.zktony.serialport

import com.zktony.serialport.command.protocol
import com.zktony.serialport.ext.toHexString
import org.junit.Assert.assertEquals
import org.junit.Test

class ProtocolTest {

    @Test
    fun test1() {
        val p = protocol {
            data = byteArrayOf(0x01, 0x06, 0x0A, 0x00, 0x00, 0x00)
        }
        assertEquals(
            "EE 01 01 06 00 01 06 0A 00 00 00 33 B9 FF FC FF FF",
            p.toByteArray().toHexString()
        )
    }

    @Test
    fun test2() {
        val p = byteArrayOf(
            0xEE.toByte(),
            0x01.toByte(),
            0x01.toByte(),
            0x00.toByte(),
            0x06.toByte(),
            0x01.toByte(),
            0x06.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x93.toByte(),
            0xD5.toByte(),
            0xFF.toByte(),
            0xFC.toByte(),
            0xFF.toByte(),
            0xFF.toByte()
        ).protocol()
        assertEquals("01 06 0A 00 00 00", p.data.toHexString())
        assertEquals("93 D5", p.crc.toHexString())
    }
}