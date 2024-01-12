package com.zktony.www.core

import com.zktony.core.ext.Ext
import com.zktony.core.ext.logi
import com.zktony.serialport.ext.intToHex
import com.zktony.serialport.protocol.toV1
import com.zktony.www.R
import com.zktony.www.core.ext.asyncHex
import com.zktony.www.core.ext.collectHex
import com.zktony.www.core.ext.decideLock
import com.zktony.www.data.dao.CalibrationDao
import com.zktony.www.data.dao.MotorDao
import com.zktony.www.data.entities.Calibration
import com.zktony.www.data.entities.Motor
import com.zktony.www.data.entities.toMotor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 14:27
 */
class ScheduleTask(
    private val motorDao: MotorDao,
    private val calibrationDao: CalibrationDao,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     *  0: X轴 1: Y轴 2: 泵1 3: 泵2 4: 泵3 5: 泵4
     */
    val hpm: MutableMap<Int, Motor> = HashMap()
    val hpc: MutableMap<Int, Float> = HashMap()

    init {
        scope.launch {
            launch {
                motorDao.getAll().collect {
                    if (it.isNotEmpty()) {
                        hpm.clear()
                        it.forEach { it1 ->
                            hpm[it1.id] = it1
                        }
                    } else {
                        motorDao.insertAll(
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
                                ),
                                Motor(
                                    id = 3,
                                    name = Ext.ctx.getString(R.string.pump_two),
                                    address = 1
                                ),
                                Motor(
                                    id = 4,
                                    name = Ext.ctx.getString(R.string.pump_three),
                                    address = 2
                                ),
                                Motor(
                                    id = 5,
                                    name = Ext.ctx.getString(R.string.pump_four),
                                    address = 3
                                )
                            )
                        )
                    }
                }
            }
            launch {
                calibrationDao.getAll().collect {
                    if (it.isNotEmpty()) {
                        it.find { c -> c.enable == 1 }?.let { c ->
                            hpc.clear()
                            hpc[0] = c.x
                            hpc[1] = c.y
                            hpc[2] = c.v1
                            hpc[3] = c.v2
                            hpc[4] = c.v3
                            hpc[5] = c.v4
                        }
                    } else {
                        calibrationDao.insert(
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
                        for (i in 0..1) {
                            for (j in 1..3) {
                                val serial = when (i) {
                                    0 -> 0
                                    else -> 3
                                }
                                asyncHex(serial) {
                                    fn = "03"
                                    pa = "04"
                                    data = j.intToHex()
                                }
                                delay(100L)
                            }
                        }
                    }
                }
            }
            launch {
                collectHex {
                    val index = it.first
                    val hex = it.second
                    if (hex != null) {
                        val v1 = hex.toV1()
                        if (v1 != null && v1.fn == "03" && v1.pa == "04") {
                            val motor = v1.data.toMotor()
                            scope.launch {
                                val id = if (index == 0) motor.address - 1 else motor.address + 2
                                motorDao.getById(id).firstOrNull()?.let { m1 ->
                                    motorDao.update(
                                        m1.copy(
                                            subdivision = motor.subdivision,
                                            speed = motor.speed,
                                            acceleration = motor.acceleration,
                                            deceleration = motor.deceleration,
                                            waitTime = motor.waitTime,
                                            mode = motor.mode,
                                        )
                                    )
                                }
                            }
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