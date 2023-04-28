package com.zktony.www.manager

import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.common.ext.*
import com.zktony.www.room.dao.CalibrationDao
import com.zktony.www.room.dao.MotorDao
import com.zktony.www.room.entity.Calibration
import com.zktony.www.room.entity.Motor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 14:27
 */
class MotorManager(
    private val MD: MotorDao,
    private val CD: CalibrationDao,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     *  0: X轴 1: Y轴 2: 泵1
     */
    private val hpm: MutableMap<Int, Motor> = HashMap()
    private val hpc: MutableMap<Int, Float> = HashMap()

    init {
        scope.launch {
            launch {
                MD.getAll().collect {
                    if (it.isNotEmpty()) {
                        hpm.clear()
                        it.forEach { it1 ->
                            hpm[it1.id] = it1
                        }
                    } else {
                        MD.insertAll(
                            listOf(
                                Motor(
                                    id = 0,
                                    name = Ext.ctx.getString(R.string.x_axis),
                                    address = 1
                                ),
                                Motor(
                                    id = 1,
                                    name = Ext.ctx.getString(R.string.y_axis),
                                    address = 2
                                ),
                                Motor(
                                    id = 2,
                                    name = Ext.ctx.getString(R.string.pump_one),
                                    address = 3
                                )
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
                            hpc[0] = c.x
                            hpc[1] = c.y
                            hpc[2] = c.v1
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
            launch {
                delay(5000L)
                decideLock {
                    no {
                        for (j in 1..3) {
                            asyncHex {
                                fn = "03"
                                pa = "04"
                                data = j.int8ToHex()
                            }
                            delay(100L)
                        }
                    }
                }
            }
            launch {

                collectHex {
                    it?.let {
                        it.toV1().run {
                            if (fn == "03" && pa == "04") {
                                val motor = data.toMotor()
                                sync(motor.copy(id = motor.address - 1))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun pulse(dv: Float, type: Int): Int {
        val me = hpm[type] ?: Motor()
        val ce = hpc[type] ?: 200f
        return me.pulseCount(dv, ce)
    }

    fun pulse(dv: List<Float>, type: List<Int>): List<Int> {
        val list = mutableListOf<Int>()
        for (i in dv.indices) {
            list.add(pulse(dv[i], type[i]))
        }
        return list
    }

    private fun sync(entity: Motor) {
        scope.launch {
            MD.getById(entity.id).firstOrNull()?.let {
                MD.update(
                    it.copy(
                        subdivision = entity.subdivision,
                        speed = entity.speed,
                        acceleration = entity.acceleration,
                        deceleration = entity.deceleration,
                        waitTime = entity.waitTime,
                        mode = entity.mode,
                    )
                )
            }
        }
    }

    fun initializer() {
        "电机管理器初始化完成！！！".logi()
    }
}