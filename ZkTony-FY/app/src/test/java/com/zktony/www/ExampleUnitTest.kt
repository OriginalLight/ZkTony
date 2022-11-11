package com.zktony.www

import com.zktony.www.common.extension.extractTemp
import com.zktony.www.common.extension.hexToAscii
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun hexToAscii() {
        val str = "43 4D 44 3A 52 45 50 4C 59 3D 32 40 30 0D"
        assertEquals("CMD:REPLY=2@0\r", str.hexToAscii())
    }

    @Test
    fun extractNumber() {
        val str = "TC1:TCACTUALTEMP=25.6@2\r"
        assertEquals("25.6", str.extractTemp())
    }
}