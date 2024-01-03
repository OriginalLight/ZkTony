package com.zktony.android

import com.zktony.android.data.entities.internal.Point
import com.zktony.android.utils.AlgorithmUtils
import com.zktony.android.utils.SerialPortUtils
import com.zktony.serialport.command.modbus.RtuProtocol
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
    fun test() {
        val points = listOf(
            Point(0.0, 0.0),
            Point(1.0, 640.0),
            Point(99.0, 64000.0),
            Point(203.0, 128000.0),
            Point(986.0, 640000.0)
        )

        val quadraticCurve = AlgorithmUtils.fitQuadraticCurve(points)

        val x = 986.0
        val y = quadraticCurve(x)

        if (y != null) {
            assertEquals(639982.8994101394, y, 1.0)
        }
    }

    @Test
    fun test1() {
        val data = ByteArray(4).writeInt16BE(201).writeInt16BE(45610, 2)
        assertEquals(data.toHexString(), "00 C9 B2 2A")

    }

    @Test
    fun test2() {
        val byte = RtuProtocol().apply {
            this.slaveAddr = (1).toByte()
            funcCode = 0x06
            data = ByteArray(4).writeInt16BE(201).writeInt16BE(45610, 2)
        }.serialization()
        assertEquals("01 06 00 C9 B2 2A 8C 3B", byte.toHexString())
    }
}