package com.zktony.www.manager

import com.zktony.core.ext.Ext
import com.zktony.core.ext.int8ToHex
import com.zktony.core.ext.logi
import com.zktony.www.R
import com.zktony.www.common.ext.toMotor
import com.zktony.www.common.ext.toV1
import com.zktony.serialport.protocol.V1
import com.zktony.www.room.dao.CalibrationDao
import com.zktony.www.room.dao.MotorDao
import com.zktony.www.room.entity.Calibration
import com.zktony.www.room.entity.Motor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 14:27
 */
class MotorManager(
    private val MD: MotorDao,
    private val CD: CalibrationDao,
    private val SM: SerialManager,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     *  0: X轴 1: Y轴 2: Z轴 3: 泵1 4: 泵2 5: 泵3 6: 泵4 7: 泵5 8: 泵6
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
                if (!SM.lock.value) {
                    for (i in 0..2) {
                        for (j in 1..3) {
                            SM.sendHex(i, V1(fn = "03", pa = "04", data = j.int8ToHex()).toHex())
                            delay(200L)
                        }
                    }
                }
            }
            launch {
                SM.ttys0Flow.collect {
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
            launch {
                SM.ttys1Flow.collect {
                    it?.let {
                        it.toV1().run {
                            if (fn == "03" && pa == "04") {
                                val motor = data.toMotor()
                                sync(motor.copy(id = motor.address + 2))
                            }
                        }
                    }
                }
            }
            launch {
                SM.ttys2Flow.collect {
                    it?.let {
                        it.toV1().run {
                            if (fn == "03" && pa == "04") {
                                val motor = data.toMotor()
                                sync(motor.copy(id = motor.address + 5))
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