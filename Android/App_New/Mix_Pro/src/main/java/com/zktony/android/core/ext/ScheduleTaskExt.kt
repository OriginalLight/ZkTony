package com.zktony.android.core.ext

import com.zktony.android.core.ScheduleTask
import com.zktony.android.data.entity.MotorEntity
import com.zktony.serialport.ext.intToHex
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.atomic.AtomicInteger

val scheduleTask: ScheduleTask by inject(ScheduleTask::class.java)


// x轴坐标
private var x: AtomicInteger = AtomicInteger(0)
private var y: AtomicInteger = AtomicInteger(0)
private var z: AtomicInteger = AtomicInteger(0)

/**
 * 脉冲
 *
 * @param index Int
 * @param dv Int
 * @return Int
 */
fun pulse(index: Int, dv: Float): Int {
    val p = (dv / scheduleTask.hpc[index]!! * 3200).toInt()
    return when (index) {
        0 -> {
            val d = p - x.get()
            x.set(maxOf(p, 0))
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
 * pulse with config
 *
 * @param index Int
 * @param dv Int
 * @return String
 */
fun pwc(index: Int, dv: Float, config: MotorEntity): String {
    return index.intToHex() + pulse(index, dv).intToHex(4) + config.hex()
}