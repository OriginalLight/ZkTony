package com.zktony.www

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zktony.common.ext.hexToAscii
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.zktony.www", appContext.packageName)
    }

    @Test
    fun hexToAscii() {
        val str = "43 4D 44 3A 52 45 50 4C 59 3D 32 40 30 0D"
        assertEquals("CMD:REPLY=2@0\r", str.hexToAscii())
    }



}