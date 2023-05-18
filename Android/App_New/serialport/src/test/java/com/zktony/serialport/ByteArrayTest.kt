package com.zktony.serialport

import com.zktony.serialport.ext.ascii2ByteArray
import com.zktony.serialport.ext.hex2ByteArray
import com.zktony.serialport.ext.readByteArrayBE
import com.zktony.serialport.ext.readFloatBE
import com.zktony.serialport.ext.readFloatLE
import com.zktony.serialport.ext.readInt16BE
import com.zktony.serialport.ext.readInt16LE
import com.zktony.serialport.ext.readInt32BE
import com.zktony.serialport.ext.readInt32LE
import com.zktony.serialport.ext.readInt8
import com.zktony.serialport.ext.readStringBE
import com.zktony.serialport.ext.readStringLE
import com.zktony.serialport.ext.readTimeBE
import com.zktony.serialport.ext.readTimeLE
import com.zktony.serialport.ext.readUInt16BE
import com.zktony.serialport.ext.readUInt16LE
import com.zktony.serialport.ext.readUInt32BE
import com.zktony.serialport.ext.readUInt32LE
import com.zktony.serialport.ext.toAsciiString
import com.zktony.serialport.ext.toHexString
import com.zktony.serialport.ext.writeStringBE
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author 刘贺贺
 * @date 2023/5/18 11:28
 */
class ByteArrayTest {

    @Test
    fun toHexString() {
        val ba = byteArrayOf(1, 2, 3, 4)
        assertEquals("01 02 03 04", ba.toHexString())
        assertEquals("01020304", ba.toHexString(false))
    }

    @Test
    fun toAsciiString() {
        val ba = byteArrayOf(0x49, 0x20, 0x4C, 0x6F, 0x76, 0x65, 0x20, 0x51, 0x4D, 0x54)
        assertEquals("I Love QMT", ba.toAsciiString())
    }

    @Test
    fun hex2ByteArray() {
        val hex = "01020304"
        val hex2 = "01 02 03 04 05"
        val ba = hex.hex2ByteArray()
        val ba2 = hex2.hex2ByteArray()
        assertEquals(0x01.toByte(), ba[0])
        assertEquals(0x02.toByte(), ba[1])
        assertEquals(0x03.toByte(), ba[2])
        assertEquals(0x04.toByte(), ba[3])
        assertEquals(0x01.toByte(), ba2[0])
        assertEquals(0x02.toByte(), ba2[1])
        assertEquals(0x03.toByte(), ba2[2])
        assertEquals(0x04.toByte(), ba2[3])
        assertEquals(0x05.toByte(), ba2[4])
    }

    @Test
    fun ascii2ByteArray() {
        val ascii = "I Love QMT"
        val ba = ascii.ascii2ByteArray(false)
        assertEquals(0x49.toByte(), ba[0])
        assertEquals(0x20.toByte(), ba[1])
        assertEquals(0x4C.toByte(), ba[2])
        assertEquals(0x6F.toByte(), ba[3])
        assertEquals(0x76.toByte(), ba[4])
        assertEquals(0x65.toByte(), ba[5])
        assertEquals(0x20.toByte(), ba[6])
        assertEquals(0x51.toByte(), ba[7])
        assertEquals(0x4D.toByte(), ba[8])
        assertEquals(0x54.toByte(), ba[9])
    }

    @Test
    fun readFloat() {
        val ba = byteArrayOf(0x41, 0x48, 0x00, 0x00)
        assertEquals(18.0f, ba.readFloatBE())
        assertEquals(18.0f, ba.reversedArray().readFloatLE())
    }

    @Test
    fun readInt8() {
        val ba = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        assertEquals(1, ba.readInt8(0))
        assertEquals(2, ba.readInt8(1))
    }

    @Test
    fun readUInt8() {
        val ba = byteArrayOf(1, -2)
        assertEquals(1, ba.readInt8(0))
        assertEquals(254, ba.readInt8(1))
    }

    @Test
    fun readInt16() {
        val ba = byteArrayOf(0, 5)
        assertEquals(5, ba.readInt16BE())
        val ba1 = byteArrayOf(1, 0, 5)
        assertEquals(5, ba1.readInt16BE(1))

        val ba2 = byteArrayOf(0x12, 0x34, 0x56)
        assertEquals(13330, ba2.readInt16LE())
    }

    @Test
    fun readUInt16() {
        val ba = byteArrayOf(0x12, 0x34, 0x56)
        assertEquals(4660, ba.readUInt16BE(0))
        assertEquals(13398, ba.readUInt16BE(1))
        assertEquals(13330, ba.readUInt16LE())
        assertEquals(22068, ba.readUInt16LE(1))
    }

    @Test
    fun readInt32() {
        val ba = byteArrayOf(0x12, 0x34, 0x56, 0x78)
        assertEquals(305419896, ba.readInt32BE())
        assertEquals(2018915346, ba.readInt32LE(0))
    }

    @Test
    fun readUInt32() {
        val ba = byteArrayOf(0x12, 0x34, 0x56, 0x78)
        assertEquals(305419896, ba.readUInt32BE(0))
        assertEquals("12345678", ba.readUInt32BE(0).toString(16))
        assertEquals(2018915346, ba.readUInt32LE(0))
        assertEquals("78563412", ba.readUInt32LE(0).toString(16))
    }

    @Test
    fun readTime() {
        val ba = byteArrayOf(0x61, 0x9B.toByte(), 0xBE.toByte(), 0x80.toByte())
        assertEquals("2021-11-23 00:00:00", ba.readTimeBE())
        assertEquals("2021-11-23", ba.readTimeBE(0, "yyyy-MM-dd"))
        assertEquals(
            "2021-11-23 00:00:00",
            ba.reversedArray().readTimeLE()
        )
        assertEquals("2021-11-23", ba.reversedArray().readTimeLE(0, "yyyy-MM-dd"))
    }

    @Test
    fun readString() {
        val ba = ByteArray(10)
        ba.writeStringBE("I Love QMT", 0, "ascii")
        assertEquals("I Love QMT", ba.readStringBE(0, 10, "ascii"))
        assertEquals(
            "49 20 4C 6F 76 65 20 51 4D 54",
            ba.readStringBE(0, 10, "hex")
        )
        assertEquals("I Love QMT", ba.reversedArray().readStringLE(0, 10, "ascii"))
        assertEquals(
            "49 20 4C 6F 76 65 20 51 4D 54",
            ba.reversedArray().readStringLE(0, 10, "hex")
        )
    }


    @Test
    fun readByteArray() {
        val ba = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09)
        val ba1 = ba.readByteArrayBE(0, 2)
        val ba2 = ba.readByteArrayBE(0, 9)
        val ba3 = ba.readByteArrayBE(2, 10)
        assertEquals("01 02", ba1.toHexString())
        assertEquals("01 02 03 04 05 06 07 08 09", ba2.toHexString())
        assertEquals("03 04 05 06 07 08 09", ba3.toHexString())
    }

}