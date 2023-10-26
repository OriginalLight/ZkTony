package com.zktony.android.utils

import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.entities.Motor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.ConcurrentHashMap

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 14:27
 */
class AsyncTask {
    private val md: MotorDao by inject(MotorDao::class.java)
    private val cd: CalibrationDao by inject(CalibrationDao::class.java)
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 电机信息
    val hpm: MutableMap<Int, Motor> = ConcurrentHashMap()

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
     *
     * 如果数据库中没有数据，则插入默认数据
     *
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
                val list = mutableListOf<Motor>()
                for (i in 0..5) {
                    if (i == 0) {
                        list.add(Motor(text = "举升2", index = i))
                    } else if (i == 1) {
                        list.add(Motor(text = "举升1", index = i))
                    } else if (i == 2) {
                        list.add(Motor(text = "夹爪", index = i))
                    } else if (i == 3) {
                        list.add(Motor(text = "泵", index = i))
                    } else if (i == 4) {
                        list.add(Motor(text = "下盘", index = i))
                    } else if (i == 5) {
                        list.add(Motor(text = "上盘", index = i))
                    }

                }
                md.insertAll(list)
            }
        }
    }

    /**
     * 从数据库中获取所有的校准信息
     *
     * 如果数据库中没有数据，则使用默认数据
     *
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
                    hpc[0] = 4.0 / 3200
                    hpc[1] = 6.35 / 3200
                    active.vps().forEachIndexed { index, vps ->
                        hpc[index + 2] = vps
                    }
                }
            } else {
                hpc.clear()
                hpc[0] = 4.0 / 3200
                hpc[1] = 6.35 / 3200
                repeat(14) { index ->
                    hpc[index + 2] = 0.01
                }
            }
        }
    }

    companion object {
        val instance: AsyncTask by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { AsyncTask() }
    }
}