package com.zktony.serialport

import com.zktony.serialport.ext.crc16
import com.zktony.serialport.ext.byteArrayToHexString
import com.zktony.serialport.protocol.toV2
import org.junit.Assert.assertEquals
import org.junit.Test

class V2Test {
    @Test
    fun crcTest() {
        // buffer 0x00 - 0xff
        val buffer = ByteArray(256)
        for (i in 0..255) {
            buffer[i] = i.toByte()
        }
        assertEquals("DE6C", buffer.crc16().byteArrayToHexString())
        assertEquals("FF9D", "EE02010100".crc16())
        assertEquals("0F9D", "EE02010200".crc16())
        assertEquals("5F5D", "EE02010301".crc16())
    }


    @Test
    fun toV2ListTest1() {
        val hex = "EE020100020100FF9DBB"
        val list = hex.toV2()
        list?.let {
            assertEquals("EE", it.head)
            assertEquals("02", it.addr)
            assertEquals("01", it.fn)
            assertEquals("0002", it.len)
            assertEquals("0100", it.data)
            assertEquals("FF9D", it.crc)
            assertEquals("BB", it.end)
            assertEquals("EE0201000201007C89BB", it.toHex())
        }
    }

}