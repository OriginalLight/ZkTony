package com.zktony.serialport

import com.zktony.serialport.ext.DataConversion
import com.zktony.serialport.ext.crc16
import com.zktony.serialport.protocol.toV2
import org.junit.Assert
import org.junit.Test

class V2Test {
    @Test
    fun crcTest() {
        // buffer 0x00 - 0xff
        val buffer = ByteArray(256)
        for (i in 0..255) {
            buffer[i] = i.toByte()
        }
        Assert.assertEquals("DE6C", DataConversion.bytesToHexString(buffer.crc16(), 0, 2))
        Assert.assertEquals("FF9D", "EE02010100".crc16())
        Assert.assertEquals("0F9D", "EE02010200".crc16())
        Assert.assertEquals("5F5D", "EE02010301".crc16())
    }


    @Test
    fun toV2ListTest1() {
        val hex = "EE02010100FF9DBB"
        val list = hex.toV2()
        list?.let {
            Assert.assertEquals("EE", it.head)
            Assert.assertEquals("02", it.addr)
            Assert.assertEquals("01", it.fn)
            Assert.assertEquals("0100", it.data)
            Assert.assertEquals("FF9D", it.crc)
            Assert.assertEquals("BB", it.end)
            Assert.assertEquals("EE02010100FF9DBB", it.toHex())
        }
    }

}