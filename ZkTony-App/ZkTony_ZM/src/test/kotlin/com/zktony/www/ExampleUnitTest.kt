package com.zktony.www

import com.zktony.www.manager.protocol.V1
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
    fun v1() {
        val v1 = V1()
        v1.motorX = 0
        v1.stepMotorX = 5000
        assertEquals("AA550101000", v1.genHex())
    }
}