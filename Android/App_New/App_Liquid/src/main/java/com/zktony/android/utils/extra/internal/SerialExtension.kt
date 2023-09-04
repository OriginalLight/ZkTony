package com.zktony.android.utils.extra.internal

import com.zktony.android.data.entities.Motor
import com.zktony.android.utils.extra.appState
import com.zktony.android.utils.extra.pulse
import com.zktony.serialport.ext.writeInt32LE
import com.zktony.serialport.ext.writeInt8

class SerialExtension {

    var controlType: Byte = 0x00
    val byteList: MutableList<Byte> = mutableListOf()
    val indexList: MutableList<Int> = mutableListOf()
    var executeType: ExecuteType = ExecuteType.SYNC
    var exceptionPolicy: ExceptionPolicy = ExceptionPolicy.SKIP
    var timeout: Long = 1000L * 10

    fun init() {
        byteList.add(0x00)
    }

    fun <T : Number> start(
        index: Int = 0,
        pdv: T,
        ads: Triple<Long, Long, Long> = Triple(
            (appState.hpm[index] ?: Motor()).acceleration,
            (appState.hpm[index] ?: Motor()).deceleration,
            (appState.hpm[index] ?: Motor()).speed
        ),
    ) {
        controlType = 0x01
        val step = pulse(index, pdv)
        if (step != 0L) {
            val config =
                Motor(acceleration = ads.first, deceleration = ads.second, speed = ads.third)
            val ba = ByteArray(5)
            ba.writeInt8(index, 0).writeInt32LE(step, 1)
            byteList.addAll(ba.toList())
            byteList.addAll(config.toByteArray().toList())
            indexList.add(index)
        }
    }

    fun stop(ids: List<Int>) {
        controlType = 0x02
        val byteArray = ByteArray(ids.size)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i, index)
        }
        byteList.addAll(byteArray.toList())
    }

    fun stop(vararg ids: Int) {
        controlType = 0x02
        val byteArray = ByteArray(ids.size)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i, index)
        }
        byteList.addAll(byteArray.toList())
    }

    fun query(ids: List<Int>) {
        controlType = 0x03
        val byteArray = ByteArray(ids.size)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i, index)
        }
        byteList.addAll(byteArray.toList())
    }

    fun query(vararg ids: Int) {
        controlType = 0x03
        val byteArray = ByteArray(ids.size)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i, index)
        }
        byteList.addAll(byteArray.toList())
    }


    fun gpio(ids: List<Int>) {
        controlType = 0x04
        val byteArray = ByteArray(ids.size)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i, index)
        }
        byteList.addAll(byteArray.toList())
    }

    fun gpio(vararg ids: Int) {
        controlType = 0x04
        val byteArray = ByteArray(ids.size)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i, index)
        }
        byteList.addAll(byteArray.toList())
    }

    fun valve(ids: List<Pair<Int, Int>>) {
        controlType = 0x05
        val byteArray = ByteArray(ids.size * 2)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i.first, index * 2)
            byteArray.writeInt8(i.second, index * 2 + 1)
        }
        byteList.addAll(byteArray.toList())
    }


    fun valve(vararg ids: Pair<Int, Int>) {
        controlType = 0x05
        val byteArray = ByteArray(ids.size * 2)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i.first, index * 2)
            byteArray.writeInt8(i.second, index * 2 + 1)
        }
        byteList.addAll(byteArray.toList())
    }
}