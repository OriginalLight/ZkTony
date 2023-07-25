package com.zktony.www.core.ext

import com.zktony.www.core.ScheduleTask
import com.zktony.www.data.entities.Motor
import org.koin.java.KoinJavaComponent.inject

val scheduleTask: ScheduleTask by inject(ScheduleTask::class.java)

/**
 * 脉冲
 *
 * @param dv Float
 * @param index Int
 * @return Int
 */
fun pulse(dv: Float, index: Int): Int {
    if (dv == 0f) return 0
    val ce = scheduleTask.hpc[index] ?: 0.01
    return (dv / ce).toInt()
}