package com.zktony.android.core.proxy

import com.zktony.android.core.ext.avgRate
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.dao.CalibrationDataDao
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.entity.Calibration
import com.zktony.android.data.entity.Motor
import com.zktony.core.ext.logi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.ConcurrentHashMap

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 14:27
 */
class MCProxy constructor(
    private val MD: MotorDao,
    private val CD: CalibrationDao,
    private val CDD: CalibrationDataDao,
) {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     *  0: Y轴 1: 泵1 2: 泵2 1: 泵3
     */
    val hpm: MutableMap<Int, Motor> = ConcurrentHashMap()
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
                        val list = mutableListOf<Motor>()
                        for (i in 0..15) {
                            list.add(Motor(text = "M$i", index = i))
                        }
                        MD.insertAll(list)
                    }
                }
            }
            launch {
                CD.getAll().collect {
                    if (it.isNotEmpty()) {
                        val c = it.find { c -> c.active == 1 }
                        if (c != null) {
                            val data = CDD.getBySubId(c.id).firstOrNull() ?: emptyList()
                            hpc.clear()
                            hpc[0] = 10f
                            hpc[1] = 10f
                            hpc[2] = 10f
                            data.avgRate().forEachIndexed { index, fl ->
                                hpc[index + 3] = fl * 100f
                            }
                        }
                    } else {
                        CD.insert(
                            Calibration(
                                name = "Default",
                                active = 1
                            )
                        )
                    }
                }
            }
        }
    }

    fun initializer() {
        "MCProxy initializer".logi()
    }
}