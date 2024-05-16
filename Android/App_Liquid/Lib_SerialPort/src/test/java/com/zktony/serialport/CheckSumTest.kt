package com.zktony.serialport

import com.zktony.serialport.ext.checkSumLE
import com.zktony.serialport.ext.toHexString
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author 刘贺贺
 * @date 2023/8/24 14:56
 */
class CheckSumTest {
    @Test
    fun checkSum() {
        val bytes = byteArrayOf(
            0xCC.toByte(),
            0x00.toByte(),
            0x44.toByte(),
            0x05.toByte(),
            0xDD.toByte(),
        )
        
        val bytes1 = byteArrayOf(
            0xCC.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0xFF.toByte(),
            0xEE.toByte(),
            0xBB.toByte(),
            0xAA.toByte(),
            0x01.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0xDD.toByte(),
        )

        assertEquals("F2 01", bytes.checkSumLE().toHexString())
        assertEquals("FC 04", bytes1.checkSumLE().toHexString())
    }
}