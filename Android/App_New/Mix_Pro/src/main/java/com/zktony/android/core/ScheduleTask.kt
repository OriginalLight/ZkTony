package com.zktony.android.core

import com.zktony.android.core.ext.avgRate
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.entity.CalibrationEntity
import com.zktony.android.data.entity.MotorEntity
import com.zktony.core.ext.logi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 14:27
 */
class ScheduleTask constructor(
    private val MD: MotorDao,
    private val CD: CalibrationDao,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     *  0: Y轴 1: 泵1 2: 泵2 1: 泵3
     */
    val hpm: MutableMap<Int, MotorEntity> = ConcurrentHashMap()
    val hpc: MutableMap<Int, Float> = ConcurrentHashMap()

    init {
        scope.launch {
            launch {
                MD.getAll().collect {
                    if (it.isNotEmpty()) {
                        hpm.clear()
                        it.forEach { it1 ->
                            hpm[it1.index] = it1
                        }
                    } else {
                        val list = mutableListOf<MotorEntity>()
                        for (i in 0..15) {
                            list.add(MotorEntity(text = "M$i", index = i))
                        }
                        MD.insertAll(list)
                    }
                }
            }
            launch {
                CD.getAll().collect {
                    if (it.isNotEmpty()) {
                        val active = it.find { c -> c.active }
                        if (active == null) {
                            CD.update(it[0].copy(active = true))
                        } else {
                            hpc.clear()
                            hpc[0] = 10f
                            hpc[1] = 10f
                            hpc[2] = 10f
                            active.avgRate().forEachIndexed { index, avgRate ->
                                hpc[index + 3] = avgRate
                            }
                        }
                    } else {
                        CD.insert(
                            CalibrationEntity(
                                name = "Default",
                                active = true,
                            )
                        )
                    }
                }
            }
        }
    }

    fun initializer() {
        "ScheduleHelper initializer".logi()
    }
}