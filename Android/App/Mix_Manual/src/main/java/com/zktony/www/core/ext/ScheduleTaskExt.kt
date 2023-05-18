package com.zktony.www.core.ext

import com.zktony.www.core.ScheduleTask
import com.zktony.www.data.entities.Motor
import org.koin.java.KoinJavaComponent.inject

val scheduleTask: ScheduleTask by inject(ScheduleTask::class.java)

/**
 * 脉冲
 *
 * @param dv Float
 * @param type Int
 * @return Int
 */
fun pulse(dv: Float, type: Int): Int {
    val me = scheduleTask.hpm[type] ?: Motor()
    val ce = scheduleTask.hpc[type] ?: 200f
    return me.pulseCount(dv, ce)
}