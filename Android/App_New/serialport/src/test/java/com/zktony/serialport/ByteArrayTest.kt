package com.zktony.serialport

import com.zktony.serialport.ext.ascii2ByteArray
import com.zktony.serialport.ext.hex2ByteArray
import com.zktony.serialport.ext.insertByteArrayBE
import com.zktony.serialport.ext.insertByteArrayLE
import com.zktony.serialport.ext.readByteArrayBE
import com.zktony.serialport.ext.readByteArrayLE
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
import com.zktony.serialport.ext.readUInt8
import com.zktony.serialport.ext.splitByteArray
import com.zktony.serialport.ext.toAsciiString
import com.zktony.serialport.ext.toHexString
import com.zktony.serialport.ext.writeByteArrayBE
import com.zktony.serialport.ext.writeByteArrayLE
import com.zktony.serialport.ext.writeFloatBE
import com.zktony.serialport.ext.writeFloatLE
import com.zktony.serialport.ext.writeInt16BE
import com.zktony.serialport.ext.writeInt16LE
import com.zktony.serialport.ext.writeInt32BE
import com.zktony.serialport.ext.writeInt32LE
import com.zktony.serialport.ext.writeInt8
import com.zktony.serialport.ext.writeStringBE
import com.zktony.serialport.ext.writeStringLE
import com.zktony.serialport.ext.writeTimeBE
import com.zktony.serialport.ext.writeTimeLE
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
        val ba = ascii.ascii2ByteArray(true)
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
        assertEquals(12.5f, ba.readFloatBE())
        assertEquals(12.5f, ba.reversedArray().readFloatLE())
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
        assertEquals(1, ba.readUInt8(0))
        assertEquals(254, ba.readUInt8(1))
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
        assertEquals("2021-11-23 00:00:00", ba.reversedArray().readTimeLE())
        assertEquals("2021-11-23", ba.reversedArray().readTimeLE(0, "yyyy-MM-dd"))
    }

    @Test
    fun readString() {
        val ba = ByteArray(10)
        ba.writeStringBE("I Love QMT", 0, "ascii")
        assertEquals("I Love QMT", ba.readStringBE(0, 10, "ascii"))
        assertEquals("49 20 4C 6F 76 65 20 51 4D 54", ba.readStringBE(0, 10, "hex"))
        assertEquals("I Love QMT", ba.reversedArray().readStringLE(0, 10, "ascii"))
        assertEquals("49 20 4C 6F 76 65 20 51 4D 54", ba.reversedArray().readStringLE(0, 10, "hex"))
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
        val ba4 = ba.readByteArrayLE(0, 2)
        val ba5 = ba.readByteArrayLE(0, 9)
        val ba6 = ba.readByteArrayLE(2, 10)
        assertEquals("02 01", ba4.toHexString())
        assertEquals("09 08 07 06 05 04 03 02 01", ba5.toHexString())
        assertEquals("09 08 07 06 05 04 03", ba6.toHexString())
    }

    @Test
    fun writeString() {
        val ba = ByteArray(15)
        ba.writeStringBE("01 02 03 04", 0, "hex")
        ba.writeStringBE("abcde", 5, "ascii")
        assertEquals("01 02 03 04 00 61 62 63 64 65 00 00 00 00 00", ba.toHexString())

        val ba1 = ByteArray(15)
        ba1.writeStringLE("01 02 03 04", 0, "hex")
        ba1.writeStringLE("abcde", 5, "ascii")
        assertEquals("04 03 02 01 00 65 64 63 62 61 00 00 00 00 00", ba1.toHexString())

        val ba2 = ByteArray(15)
        ba2.writeStringBE("01 02 03 04", 0, 2)
        ba2.writeStringBE("abcde", 5, 3, "ascii")
        assertEquals("01 02 00 00 00 61 62 63 00 00 00 00 00 00 00", ba2.toHexString())

        val ba3 = ByteArray(15)
        ba3.writeStringLE("01 02 03 04", 0, 2)
        ba3.writeStringLE("abcde", 5, 3, "ascii")
        assertEquals("04 03 00 00 00 65 64 63 00 00 00 00 00 00 00", ba3.toHexString())
    }

    @Test
    fun writeFloat() {
        val ba = ByteArray(15)
        ba.writeFloatBE(3.1415926f)
        assertEquals("40 49 0F DA 00 00 00 00 00 00 00 00 00 00 00", ba.toHexString())
        assertEquals(3.1415925f, ba.readFloatBE())

        val ba1 = ByteArray(15)
        ba1.writeFloatLE(3.1415926f)
        assertEquals("DA 0F 49 40 00 00 00 00 00 00 00 00 00 00 00", ba1.toHexString())
        assertEquals(3.1415925f, ba1.readFloatLE())
    }

    @Test
    fun writeInt8() {
        val ba = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09)
        assertEquals("0A 02 03 04 05 06 07 08 09", ba.writeInt8(10).toHexString())
        assertEquals("0A 0B 03 04 05 06 07 08 09", ba.writeInt8(11, 1).toHexString())
    }

    @Test
    fun writeInt16() {
        val ba = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09)
        assertEquals("00 0A 03 04 05 06 07 08 09", ba.writeInt16BE(10).toHexString())
        assertEquals("00 0A 03 00 0B 06 07 08 09", ba.writeInt16BE(11, 3).toHexString())

        val ba1 = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09)
        assertEquals("0A 00 03 04 05 06 07 08 09", ba1.writeInt16LE(10).toHexString())
        assertEquals("0A 00 03 0B 00 06 07 08 09", ba1.writeInt16LE(11, 3).toHexString())
    }

    @Test
    fun writeInt32() {
        val ba = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09)
        assertEquals("00 00 00 0A 05 06 07 08 09", ba.writeInt32BE(10).toHexString())
        assertEquals("00 00 00 0A 00 00 00 0B 09", ba.writeInt32BE(11, 4).toHexString())

        val ba1 = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09)
        assertEquals("0A 00 00 00 05 06 07 08 09", ba1.writeInt32LE(10).toHexString())
        assertEquals("0A 00 00 00 0B 00 00 00 09", ba1.writeInt32LE(11, 4).toHexString())
    }

    @Test
    fun writeTime() {
        val ba = ByteArray(15)
        ba.writeTimeBE("2021-11-23 14:20:52")
        assertEquals("61 9C 88 44 00 00 00 00 00 00 00 00 00 00 00", ba.toHexString())
        assertEquals("2021-11-23 14:20:52", ba.readTimeBE())

        val ba1 = ByteArray(15)
        ba1.writeTimeLE("2021-11-23 14:20:52")
        assertEquals("44 88 9C 61 00 00 00 00 00 00 00 00 00 00 00", ba1.toHexString())
        assertEquals("2021-11-23 14:20:52", ba1.readTimeLE())
    }

    @Test
    fun writeByteArray() {
        val ba = ByteArray(15)
        ba.writeByteArrayBE(byteArrayOf(0x01, 0x02, 0x03, 0x04))
        assertEquals("01 02 03 04 00 00 00 00 00 00 00 00 00 00 00", ba.toHexString())

        ba.writeByteArrayBE(byteArrayOf(0x05, 0x06, 0x07, 0x08), 4, 2)
        assertEquals("01 02 03 04 05 06 00 00 00 00 00 00 00 00 00", ba.toHexString())


        val ba1 = ByteArray(15)
        ba1.writeByteArrayLE(byteArrayOf(0x01, 0x02, 0x03, 0x04))
        assertEquals("04 03 02 01 00 00 00 00 00 00 00 00 00 00 00", ba1.toHexString())

        ba1.writeByteArrayLE(byteArrayOf(0x05, 0x06, 0x07, 0x08), 4, 2)
        assertEquals("04 03 02 01 08 07 00 00 00 00 00 00 00 00 00", ba1.toHexString())
    }

    @Test
    fun insertByteArray() {
        val ba = ByteArray(15)
        val insertArray = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06)
        val result = ba.insertByteArrayBE(insertArray, 1, 1)
        assertEquals(
            "00 02 03 04 05 06 00 00 00 00 00 00 00 00 00 00 00 00 00 00",
            result.toHexString()
        )

        val ba1 = ByteArray(15)
        val insertArray1 = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06)
        val result1 = ba1.insertByteArrayLE(insertArray1, 1, 1)
        assertEquals(
            "00 05 04 03 02 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00",
            result1.toHexString()
        )
    }

    @Test
    fun splitByteArray() {
        val ba = byteArrayOf(
            0xEE.toByte(),
            0x01.toByte(),
            0x01.toByte(),
            0x00.toByte(),
            0x01.toByte(),
            0xFF.toByte(),
            0xFC.toByte(),
            0xFF.toByte(),
            0xFF.toByte()
        )
        val ba1 = byteArrayOf(
            0xEE.toByte(),
            0x01.toByte(),
            0x01.toByte(),
            0x00.toByte(),
            0x01.toByte(),
            0xFF.toByte(),
            0xFC.toByte(),
            0xFF.toByte(),
            0xFF.toByte()
        )
        val ba2 = byteArrayOf(
            0xEE.toByte(),
            0x01.toByte(),
            0x01.toByte(),
            0x00.toByte(),
            0x01.toByte(),
            0xFF.toByte(),
            0xFC.toByte(),
            0xFF.toByte(),
            0xFF.toByte()
        )

        val result = (ba + ba1 + ba2).splitByteArray(
            byteArrayOf(0xEE.toByte()),
            byteArrayOf(
                0xFF.toByte(),
                0xFC.toByte(),
                0xFF.toByte(),
                0xFF.toByte()
            )
        )
        assertEquals(3, result.size)
    }
}