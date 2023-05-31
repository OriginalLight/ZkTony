package com.zktony.android.logic.ext

import com.zktony.android.logic.ScheduleTask
import com.zktony.android.logic.data.entities.MotorEntity
import com.zktony.serialport.ext.writeInt32LE
import com.zktony.serialport.ext.writeInt8
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.atomic.AtomicLong

val scheduleTask: ScheduleTask by inject(ScheduleTask::class.java)


// x轴坐标
private var x: AtomicLong = AtomicLong(0L)
private var y: AtomicLong = AtomicLong(0L)
private var z: AtomicLong = AtomicLong(0L)

/**
 * 脉冲
 *
 * @param index Int
 * @param dv Int
 * @return Int
 */
fun pulse(index: Int, dv: Float): Long {
    val p = (dv / scheduleTask.hpc[index]!!).toLong()
    return when (index) {
        0 -> {
            val d = p - x.get()
            x.set(maxOf(p, 0L))
            d
        }

        1 -> {
            val d = p - y.get()
            y.set(maxOf(p, 0))
            d
        }

        2 -> {
            val d = p - z.get()
            z.set(maxOf(p, 0))
            d
        }

        else -> p
    }
}

/**
 * distance or volume pulse with config
 *
 * @param index Int
 * @param dv Int
 * @return ByteArray
 */
fun pwc(index: Int, dv: Float, config: MotorEntity): ByteArray {
    val ba = ByteArray(5)
    return ba.writeInt8(index, 0).writeInt32LE(pulse(index, dv), 1) + config.toByteArray()
}

/**
 * pulse with config
 *
 * @param index Int
 * @param pulse Long
 * @return ByteArray
 */
fun pwc(index: Int, pulse: Long, config: MotorEntity): ByteArray {
    val ba = ByteArray(5)
    return ba.writeInt8(index, 0).writeInt32LE(pulse, 1) + config.toByteArray()
}