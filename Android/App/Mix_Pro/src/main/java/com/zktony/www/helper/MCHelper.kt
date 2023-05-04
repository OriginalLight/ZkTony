package com.zktony.www.helper

import com.zktony.core.ext.Ext
import com.zktony.core.ext.logi
import com.zktony.www.R
import com.zktony.www.room.dao.CalibrationDao
import com.zktony.www.room.dao.MotorDao
import com.zktony.www.room.entity.Calibration
import com.zktony.www.room.entity.Motor
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 14:27
 */
class MCHelper constructor(
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
                        MD.insertAll(
                            listOf(
                                Motor(text = Ext.ctx.getString(R.string.x_axis), index = 0),
                                Motor(text = Ext.ctx.getString(R.string.pump_one), index = 1),
                                Motor(text = Ext.ctx.getString(R.string.pump_two), index = 2),
                                Motor(text = Ext.ctx.getString(R.string.pump_three), index = 3),
                            )
                        )
                    }
                }
            }
            launch {
                CD.getAll().collect {
                    if (it.isNotEmpty()) {
                        it.find { c -> c.enable == 1 }?.let { c ->
                            hpc.clear()
                            hpc[0] = c.v1
                            hpc[1] = c.v2
                            hpc[2] = c.v3
                        }
                    } else {
                        CD.insert(
                            Calibration(
                                name = Ext.ctx.getString(com.zktony.core.R.string.def),
                                enable = 1
                            )
                        )
                    }
                }
            }
        }
    }

    fun init() {
        "初始化MCHelper".logi()
    }
}