package com.zktony.android.utils.extra.internal

import com.zktony.android.data.entities.Motor
import com.zktony.android.utils.extra.appState
import com.zktony.android.utils.extra.pulse
import com.zktony.serialport.ext.writeInt32LE
import com.zktony.serialport.ext.writeInt8

class SerialExtension {

    var controlType: Byte = ControlType.RESET
    val byteList: MutableList<Byte> = mutableListOf()
    val indexList: MutableList<Int> = mutableListOf()
    var executeType: Int = ExecuteType.SYNC
    var exceptionPolicy: Int = ExceptionPolicy.SKIP
    var timeout: Long = 1000L * 10

    fun init() {
        byteList.add(0x00)
    }

    fun <T : Number> start(
        index: Int = 0,
        pdv: T,
        ads: Triple<Long, Long, Long>? = null,
    ) {
        controlType = ControlType.START
        val step = pulse(index, pdv)
        if (step != 0L) {
            val ba1 = ByteArray(5)
            val ba2 = ByteArray(12)
            ba1.writeInt8(index, 0).writeInt32LE(step, 1)
            if (ads == null) {
                val motor = appState.hpm[index] ?: Motor()
                ba2.writeInt32LE(motor.acceleration, 0).writeInt32LE(motor.deceleration, 4)
                    .writeInt32LE(motor.speed, 8)
            } else {
                ba2.writeInt32LE(ads.first, 0).writeInt32LE(ads.second, 4)
                    .writeInt32LE(ads.third, 8)
            }
            byteList.addAll(ba1.toList())
            byteList.addAll(ba2.toList())
            indexList.add(index)
        }
    }

    fun stop(ids: List<Int>) = stop(*ids.toIntArray())

    fun stop(vararg ids: Int) {
        controlType = ControlType.STOP
        val byteArray = ByteArray(ids.size)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i, index)
        }
        byteList.addAll(byteArray.toList())
    }

    fun query(ids: List<Int>) = query(*ids.toIntArray())

    fun query(vararg ids: Int) {
        controlType = ControlType.QUERY
        val byteArray = ByteArray(ids.size)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i, index)
        }
        byteList.addAll(byteArray.toList())
    }

    fun gpio(ids: List<Int>) = gpio(*ids.toIntArray())

    fun gpio(vararg ids: Int) {
        controlType = ControlType.GPIO
        val byteArray = ByteArray(ids.size)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i, index)
        }
        byteList.addAll(byteArray.toList())
    }

    fun valve(ids: List<Pair<Int, Int>>) = valve(*ids.toTypedArray())

    fun valve(vararg ids: Pair<Int, Int>) {
        controlType = ControlType.VALVE
        val byteArray = ByteArray(ids.size * 2)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i.first, index * 2)
            byteArray.writeInt8(i.second, index * 2 + 1)
        }
        byteList.addAll(byteArray.toList())
    }
}