package com.zktony.serialport

import com.zktony.serialport.ext.DataConversion
import org.junit.Assert.assertEquals
import org.junit.Test

class HexTest {

    @Test
    fun spiltTest() {
        val text = "EE02010100FF9DBB"
        val list = DataConversion.splitString(text, "EE", "BB")
        assertEquals("EE02010100FF9DBB", list[0])
    }

    @Test
    fun spiltTest2() {
        val text = "EE02010100FF9DBBEE03010100FF9DBB"
        val list = DataConversion.splitString(text, "EE", "BB")
        assertEquals("EE02010100FF9DBB", list[0])
        assertEquals("EE03010100FF9DBB", list[1])
    }

    @Test
    fun spiltTest3() {
        val text = "02010100FF"
        val list = DataConversion.splitString(text, "EE", "BB")
        assertEquals("02010100FF", list[0])
    }

    @Test
    fun spiltTest4() {
        val text = "EE02010100FF"
        val list = DataConversion.splitString(text, "EE", "BB")
        assertEquals("EE02010100FF", list[0])
    }

    @Test
    fun spiltTest5() {
        val text = "02010100FFBB02010100FFBB"
        val list = DataConversion.splitString(text, "EE", "BB")
        assertEquals("02010100FFBB02010100FFBB", list[0])
    }

    @Test
    fun spiltTest6() {
        val text = "EE02010100FFBB02010100FFFCFFFF"
        val list = DataConversion.splitString(text, "EE", "FFFCFFFF")
        assertEquals("EE02010100FFBB02010100FFFCFFFF", list[0])
    }

    @Test
    fun spiltTest7() {
        val text = "EE02010100FFBB02010100FFFCFFFFEE02010100FFBB02010100FFFCFFFF"
        val list = DataConversion.splitString(text, "EE", "FFFCFFFF")
        assertEquals("EE02010100FFBB02010100FFFCFFFF", list[0])
        assertEquals("EE02010100FFBB02010100FFFCFFFF", list[1])
    }
}