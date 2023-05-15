package com.zktony.www.proxy

import com.zktony.core.ext.*
import com.zktony.serialport.ext.intToHex
import com.zktony.serialport.protocol.toV1
import com.zktony.www.R
import com.zktony.www.common.ext.*
import com.zktony.www.room.dao.CalibrationDao
import com.zktony.www.room.dao.MotorDao
import com.zktony.www.room.entity.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 14:27
 */
class MCProxy(
    private val MD: MotorDao,
    private val CD: CalibrationDao,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     *  0: X轴 1: Y轴 2: Z轴 3: 泵1 4: 泵2 5: 泵3 6: 泵4 7: 泵5 8: 泵6
     */
    val hpm: MutableMap<Int, Motor> = HashMap()
    val hpc: MutableMap<Int, Float> = HashMap()

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
                        val list = mutableListOf<Motor>()
                        list.add(
                            Motor(
                                id = 0,
                                name = Ext.ctx.getString(R.string.x_axis),
                                address = 1
                            )
                        )
                        list.add(
                            Motor(
                                id = 1,
                                name = Ext.ctx.getString(R.string.y_axis),
                                address = 2
                            )
                        )
                        list.add(
                            Motor(
                                id = 2,
                                name = Ext.ctx.getString(R.string.z_axis),
                                address = 3
                            )
                        )
                        list.add(
                            Motor(
                                id = 3,
                                name = Ext.ctx.getString(R.string.pump_one),
                                address = 1
                            )
                        )
                        list.add(
                            Motor(
                                id = 4,
                                name = Ext.ctx.getString(R.string.pump_two),
                                address = 2
                            )
                        )
                        list.add(
                            Motor(
                                id = 5,
                                name = Ext.ctx.getString(R.string.pump_three),
                                address = 3
                            )
                        )
                        list.add(
                            Motor(
                                id = 6,
                                name = Ext.ctx.getString(R.string.pump_four),
                                address = 1
                            )
                        )
                        list.add(
                            Motor(
                                id = 7,
                                name = Ext.ctx.getString(R.string.pump_five),
                                address = 2
                            )
                        )
                        list.add(
                            Motor(
                                id = 8,
                                name = Ext.ctx.getString(R.string.pump_six),
                                address = 3
                            )
                        )
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
                        for (i in 0..2) {
                            for (j in 1..3) {
                                asyncHex(i) {
                                    fn = "03"
                                    pa = "04"
                                    data = j.intToHex()
                                }
                                delay(200L)
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
                        if (v1 != null) {
                            if (v1.fn == "03" && v1.pa == "04") {
                                val motor = v1.data.toMotor()
                                val id = when (index) {
                                    0 -> motor.address - 1
                                    1 -> motor.address + 2
                                    2 -> motor.address + 5
                                    else -> 0
                                }
                                scope.launch {
                                    MD.getById(id).firstOrNull()?.let { m1 ->
                                        MD.update(
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
    }


    fun initializer() {
        "MCProxy initializer".logi()
    }
}