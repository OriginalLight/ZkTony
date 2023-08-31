package com.zktony.android

import com.zktony.android.utils.extra.Point
import com.zktony.android.utils.extra.fitQuadraticCurve
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
    fun test() {
        val points = listOf(
            Point(0.0, 0.0),
            Point(1.0, 640.0),
            Point(99.0, 64000.0),
            Point(203.0, 128000.0),
            Point(986.0, 640000.0)
        )

        val quadraticCurve = fitQuadraticCurve(points)

        val x = 986.0
        val y = quadraticCurve(x)

        if (y != null) {
            assertEquals(639982.8994101394, y, 1.0)
        }
    }
}