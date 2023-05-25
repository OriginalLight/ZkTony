package com.zktony.www.core.ext

import com.zktony.www.core.ScheduleTask
import com.zktony.www.core.WorkerManager
import com.zktony.www.data.entities.Motor
import org.koin.java.KoinJavaComponent

val workerManager: WorkerManager by KoinJavaComponent.inject(WorkerManager::class.java)
val scheduleTask: ScheduleTask by KoinJavaComponent.inject(ScheduleTask::class.java)

/**
 * 脉冲
 *
 * @param dv Float
 * @param type Int
 * @return Int
 */
fun pulse(dv: Float, type: Int): Int {
    val vps = scheduleTask.hpc[type] ?: (150f / 3200f)
    return (dv / vps).toInt()
}

/**
 * 脉冲
 *
 * @param dv List<Float>
 * @param type List<Int>
 * @return List<Int>
 */
fun pulse(dv: List<Float>, type: List<Int>): List<Int> {
    val list = mutableListOf<Int>()
    for (i in dv.indices) {
        list.add(pulse(dv[i], type[i]))
    }
    return list
}