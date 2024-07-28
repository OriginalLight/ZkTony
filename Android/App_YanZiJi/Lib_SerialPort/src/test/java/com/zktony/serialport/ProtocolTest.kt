package com.zktony.serialport

import com.zktony.serialport.protocol.ZktyProtocol
import com.zktony.serialport.ext.toHexString
import org.junit.Assert.assertEquals
import org.junit.Test

class ProtocolTest {

    @Test
    fun test1() {
        val p = ZktyProtocol().apply {
            data = byteArrayOf(0x01, 0x06, 0x0A, 0x00, 0x00, 0x00)
        }
        assertEquals(
            "EE 01 01 01 06 00 01 06 0A 00 00 00 5D A1 FC FF",
            p.toByteArray().toHexString()
        )
    }

}