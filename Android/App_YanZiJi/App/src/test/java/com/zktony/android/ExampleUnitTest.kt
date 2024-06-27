package com.zktony.android

import com.zktony.serialport.ext.toHexString
import com.zktony.serialport.ext.writeInt16BE
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList

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
    fun copyOnWriteArrayList() {
        val list = CopyOnWriteArrayList<Int>()
        for (i in 0..10) {
            list.add(i)
        }
        for (i in 0..10) {
            list[i] = list[i] + 1
        }
        assertEquals(11, list.size)
        assertEquals(1, list[0])
        assertEquals(11, list[10])
    }

    @Test
    fun test1() {
        val data = ByteArray(4).writeInt16BE(201).writeInt16BE(45610, 2)
        assertEquals(data.toHexString(), "00 C9 B2 2A")

    }
}