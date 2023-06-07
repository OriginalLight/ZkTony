package com.zktony.android.logic

import com.zktony.android.logic.data.dao.CalibrationDao
import com.zktony.android.logic.data.dao.MotorDao
import com.zktony.android.logic.data.entities.MotorEntity
import com.zktony.core.ext.logi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 14:27
 */
class ScheduleTask constructor(
    private val motorDao: MotorDao,
    private val calibrationDao: CalibrationDao,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     *  0: Y轴 1: 泵1 2: 泵2 1: 泵3
     */
    val hpm: MutableMap<Int, MotorEntity> = ConcurrentHashMap()
    val hpc: MutableMap<Int, Double> = ConcurrentHashMap()

    init {
        scope.launch {
            launch {
                motorDao.getAll().collect {
                    if (it.isNotEmpty()) {
                        it.forEach { it1 ->
                            hpm[it1.index] = it1
                        }
                    } else {
                        val list = mutableListOf<MotorEntity>()
                        for (i in 0..15) {
                            list.add(MotorEntity(text = "M$i", index = i))
                        }
                        motorDao.insertAll(list)
                    }
                }
            }
            launch {
                calibrationDao.getAll().collect {
                    if (it.isNotEmpty()) {
                        val active = it.find { c -> c.active }
                        if (active == null) {
                            calibrationDao.update(it[0].copy(active = true))
                        } else {
                            hpc.clear()
                            hpc[0] = 10.0 / 3200
                            hpc[1] = 10.0 / 3200
                            hpc[2] = 10.0 / 3200
                            active.vps().forEachIndexed { index, vps ->
                                hpc[index + 3] = vps
                            }
                        }
                    } else {
                        hpc.clear()
                        hpc[0] = 10.0 / 3200
                        hpc[1] = 10.0 / 3200
                        hpc[2] = 10.0 / 3200
                        repeat(13) { index ->
                            hpc[index + 3] = 0.01
                        }
                    }
                }
            }
        }
    }


    fun initializer() {
        "ScheduleTask initializer".logi()
    }
}