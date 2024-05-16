package com.zktony.serialport

import com.zktony.serialport.ext.*
import org.junit.Assert.assertEquals
import org.junit.Test

class HexTest {

    @Test
    fun spiltTest() {
        val text = "EE02010100FF9DBB"
        val list = text.splitString("EE", "BB")
        assertEquals("EE02010100FF9DBB", list[0])
    }

    @Test
    fun spiltTest2() {
        val text = "EE02010100FF9DBBEE03010100FF9DBB"
        val list = text.splitString("EE", "BB")
        assertEquals("EE02010100FF9DBB", list[0])
        assertEquals("EE03010100FF9DBB", list[1])
    }

    @Test
    fun spiltTest3() {
        val text = "02010100FF"
        val list = text.splitString("EE", "BB")
        assertEquals("02010100FF", list[0])
    }

    @Test
    fun spiltTest4() {
        val text = "EE02010100FF"
        val list = text.splitString("EE", "BB")
        assertEquals("EE02010100FF", list[0])
    }

    @Test
    fun spiltTest5() {
        val text = "02010100FFBB02010100FFBB"
        val list = text.splitString("EE", "BB")
        assertEquals("02010100FFBB02010100FFBB", list[0])
    }

    @Test
    fun spiltTest6() {
        val text = "EE02010100FFBB02010100FFFCFFFF"
        val list = text.splitString("EE", "FFFCFFFF")
        assertEquals("EE02010100FFBB02010100FFFCFFFF", list[0])
    }

    @Test
    fun spiltTest7() {
        val text = "EE02010100FFBB02010100FFFCFFFFEE02010100FFBB02010100FFFCFFFF"
        val list = text.splitString("EE", "FFFCFFFF")
        assertEquals("EE02010100FFBB02010100FFFCFFFF", list[0])
        assertEquals("EE02010100FFBB02010100FFFCFFFF", list[1])
    }

    @Test
    fun intToHexTest1() {
        val int = 200
        val hex = int.intToHex(1)
        assertEquals("C8", hex)
    }

    @Test
    fun intToHexTest2() {
        val int = 200
        val hex = int.intToHex(2)
        assertEquals("00C8", hex)
    }

    @Test
    fun intToHexTest3() {
        val int = 200
        val hex = int.intToHex(4)
        assertEquals("000000C8", hex)
    }

    @Test
    fun intToHexTest4() {
        val int = 200
        val hex = int.intToHex(8)
        assertEquals("00000000000000C8", hex)
    }

    @Test
    fun intToHexTest5() {
        val int = -160
        val hex = int.intToHex(4)
        assertEquals("FFFFFF60", hex)
    }

    @Test
    fun hexToIntTest1() {
        val hex = "C8"
        val int = hex.hexToInt()
        assertEquals(200, int)
    }

    @Test
    fun hexToIntTest2() {
        val hex = "00C8"
        val int = hex.hexToInt()
        assertEquals(200, int)
    }

    @Test
    fun hexToIntTest3() {
        val hex = "000000C8"
        val int = hex.hexToInt()
        assertEquals(200, int)
    }

    @Test
    fun hexToIntTest4() {
        val hex = "00000000000000C8"
        val int = hex.hexToInt()
        assertEquals(200, int)
    }

    @Test
    fun hexToIntTest5() {
        val hex = "FFFFFF60"
        val int = hex.hexToInt()
        assertEquals(-160, int)
    }

    @Test
    fun bytesToHex() {
        val bytes = byteArrayOf(
            0x00.toByte(),
            0x01.toByte(),
            0x02.toByte(),
            0x03.toByte(),
            0xFF.toByte(),
        )
        assertEquals("00010203FF", bytes.byteArrayToHexString())
    }

    @Test
    fun hexToBytes() {
        val hex = "00010203FF"
        val bytes = hex.hexStringToByteArray()
        assertEquals(0x00.toByte(), bytes[0])
        assertEquals(0x01.toByte(), bytes[1])
        assertEquals(0x02.toByte(), bytes[2])
        assertEquals(0x03.toByte(), bytes[3])
        assertEquals(0xFF.toByte(), bytes[4])
    }

    @Test
    fun stringToHex() {
        val text = "100,3.4,5"
        val hex = text.asciiToHex()
        assertEquals("3130302C332E342C35", hex)
        assertEquals("100,3.4,5", hex.hexToAscii())
        assertEquals("3130302C332E342C35", text.toByteArray().byteArrayToHexString())
    }
}