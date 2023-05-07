package com.zktony.android.core.proxy

import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.entity.Calibration
import com.zktony.android.data.entity.Motor
import com.zktony.core.R
import com.zktony.core.ext.Ext
import com.zktony.core.ext.logi
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 14:27
 */
class MCProxy constructor(
    private val MD: MotorDao,
    private val CD: CalibrationDao,
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
                        it.find { c -> c.enable == 1 }?.let { c ->
                            hpc.clear()
                            hpc[0] = c.x
                            hpc[1] = c.y
                            hpc[2] = c.z
                            hpc[3] = c.v1
                            hpc[4] = c.v2
                            hpc[5] = c.v3
                            hpc[6] = c.v4
                            hpc[7] = c.v5
                            hpc[8] = c.v6
                            hpc[9] = c.v7
                            hpc[10] = c.v8
                            hpc[11] = c.v9
                            hpc[12] = c.v10
                            hpc[13] = c.v11
                            hpc[14] = c.v12
                            hpc[15] = c.v13
                        }
                    } else {
                        CD.insert(
                            Calibration(
                                name = "Default",
                                enable = 1
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