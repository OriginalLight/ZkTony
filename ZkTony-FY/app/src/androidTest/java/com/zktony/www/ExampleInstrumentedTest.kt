package com.zktony.www

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zktony.www.common.extension.extractTemp
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
    fun extractNumber() {
        val str = "TC1:TCACTUALTEMP=25.6@2"
        assertEquals("25.6", str.extractTemp())
    }
}