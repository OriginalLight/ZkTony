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
    private val md: MotorDao,
    private val cd: CalibrationDao,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 电机信息
    val hpm: MutableMap<Int, MotorEntity> = ConcurrentHashMap()

    // 校准信息
    val hpc: MutableMap<Int, Double> = ConcurrentHashMap()

    init {
        scope.launch {
            launch {
                asyncTaskOne()
            }
            launch {
                asyncTaskTwo()
            }
        }
    }

    /**
     * 从数据库中获取所有的电机信息
     * 如果数据库中没有数据，则插入默认数据
     * 如果数据库中有数据，则将数据存入hpm中
     *
     * @return Unit
     */
    private suspend fun asyncTaskOne() {
        md.getAll().collect {
            if (it.isNotEmpty()) {
                it.forEach { it1 ->
                    hpm[it1.index] = it1
                }
            } else {
                val list = mutableListOf<MotorEntity>()
                for (i in 0..15) {
                    list.add(MotorEntity(text = "M$i", index = i))
                }
                md.insertAll(list)
            }
        }
    }

    /**
     * 从数据库中获取所有的校准信息
     * 如果数据库中没有数据，则使用默认数据
     * 如果数据库中有数据，则将数据存入hpc中
     *
     * @return Unit
     */
    private suspend fun asyncTaskTwo() {
        cd.getAll().collect {
            if (it.isNotEmpty()) {
                val active = it.find { c -> c.active }
                if (active == null) {
                    cd.update(it[0].copy(active = true))
                } else {
                    hpc.clear()
                    hpc[0] = 10.0 / 3200
                    hpc[1] = 10.0 / 3200
                    active.vps().forEachIndexed { index, vps ->
                        hpc[index + 3] = vps
                    }
                }
            } else {
                hpc.clear()
                hpc[0] = 10.0 / 3200
                hpc[1] = 10.0 / 3200
                repeat(14) { index ->
                    hpc[index + 2] = 0.01
                }
            }
        }
    }


    fun initializer() {
        "ScheduleTask initializer".logi()
    }
}