package com.zktony.serialport

import com.zktony.serialport.command.Protocol
import com.zktony.serialport.ext.toHexString
import org.junit.Assert.assertEquals
import org.junit.Test

class ProtocolTest {

    @Test
    fun test1() {
        val p = Protocol().apply {
            data = byteArrayOf(0x01, 0x06, 0x0A, 0x00, 0x00, 0x00)
        }
        assertEquals(
            "EE 01 01 06 00 01 06 0A 00 00 00 33 B9 FF FC FF FF",
            p.toByteArray().toHexString()
        )
    }

}