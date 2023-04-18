package com.zktony.www

import junit.framework.TestCase.assertEquals
import org.junit.Test

class HexUnitTest {
    @Test
    fun to_byte() {
        val hex = "0101"
        val bytes = hex.toByteArray()
        assertEquals(4, bytes.size)
    }
}