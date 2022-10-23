package com.zktony.www

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zktony.www.common.extension.*
import com.zktony.www.data.entity.Motor
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@Suppress("NonAsciiCharacters")
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.zktony.www", appContext.packageName)
    }

    @Test
    fun Int转一个的Hex() {
        assertEquals("AA", 170.int8ToHex())
        assertEquals("55", 85.int8ToHex())
        assertEquals("00", 0.int8ToHex())
        assertEquals("01", 1.int8ToHex())
        assertEquals("02", 2.int8ToHex())
    }

    @Test
    fun 一个的Hex转Int() {
        assertEquals(170, "AA".hexToInt8())
        assertEquals(85, "55".hexToInt8())
        assertEquals(0, "00".hexToInt8())
        assertEquals(1, "01".hexToInt8())
        assertEquals(2, "02".hexToInt8())
    }

    @Test
    fun Int转两个的Hex() {
        assertEquals("0000", 0.int16ToHex2())
        assertEquals("0001", 1.int16ToHex2())
        assertEquals("0002", 2.int16ToHex2())
        assertEquals("0003", 3.int16ToHex2())
        assertEquals("0064", 100.int16ToHex2())
    }

    @Test
    fun 两个的Hex转Int() {
        assertEquals(0, "0000".hex2ToInt16())
        assertEquals(1, "0001".hex2ToInt16())
        assertEquals(2, "0002".hex2ToInt16())
        assertEquals(3, "0003".hex2ToInt16())
        assertEquals(100, "0064".hex2ToInt16())
    }

    @Test
    fun Int转四个的Hex() {
        assertEquals("000000AA", 170.int32ToHex4())
        assertEquals("00000055", 85.int32ToHex4())
        assertEquals("00000000", 0.int32ToHex4())
        assertEquals("00000001", 1.int32ToHex4())
        assertEquals("00000002", 2.int32ToHex4())
    }

    @Test
    fun 四个的Hex转Int() {
        assertEquals(170, "000000AA".hex4ToInt32())
        assertEquals(85, "00000055".hex4ToInt32())
        assertEquals(0, "00000000".hex4ToInt32())
        assertEquals(1, "00000001".hex4ToInt32())
        assertEquals(2, "00000002".hex4ToInt32())
    }

    @Test
    fun Float转4个的Hex() {
        assertEquals("41F00000", 30.0f.float32ToHex4())
        assertEquals("41C80000", 25.0f.float32ToHex4())
        assertEquals("420C0000", 35.0f.float32ToHex4())
    }

    @Test
    fun 四个的Hex转Float() {
        assertEquals(30.0f, "41F00000".hex4ToFloat32())
        assertEquals(25.0f, "41C80000".hex4ToFloat32())
        assertEquals(35.0f, "420C0000".hex4ToFloat32())
    }

    @Test
    fun 高低位() {
        assertEquals("AA55", "55AA".hexHighLow())
        assertEquals("AA554433", "334455AA".hexHighLow())
        assertEquals("AA5544332211", "1122334455AA".hexHighLow())
        assertEquals("AA55AA55AA55AA55", "55AA55AA55AA55AA".hexHighLow())
    }

    @Test
    fun Hex分割() {
        val hex = "EE0185010001000100FFFCFFFFEE01060A00FFFCFFFF"
        val list = hex.splitHex().filter { it.isNotEmpty() }
        assertEquals(2, list.size)
    }

    @Test
    fun 电机参数反序列化() {
        val motor = Motor("011000643C3C010000")
        assertEquals(1, motor.address)
        assertEquals(16, motor.subdivision)
        assertEquals(100, motor.speed)
        assertEquals(60, motor.acceleration)
        assertEquals(60, motor.deceleration)
        assertEquals(1, motor.mode)
        assertEquals(0, motor.waitTime)

    }
}