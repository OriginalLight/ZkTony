package com.zktony.www.manager

import com.zktony.core.ext.Ext
import com.zktony.core.ext.int8ToHex
import com.zktony.core.ext.logi
import com.zktony.www.R
import com.zktony.www.common.ext.toMotor
import com.zktony.www.common.ext.toV1
import com.zktony.www.manager.protocol.V1
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
class MotorManager constructor(
    private val MD: MotorDao,
    private val CD: CalibrationDao,
    private val SM: SerialManager,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     *  0: 泵1 1: 泵2 2: 泵3
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
                                    name = Ext.ctx.getString(R.string.pump_one),
                                    address = 1
                                ),
                                Motor(
                                    id = 1,
                                    name = Ext.ctx.getString(R.string.pump_two),
                                    address = 2
                                ),
                                Motor(
                                    id = 2,
                                    name = Ext.ctx.getString(R.string.pump_three),
                                    address = 3
                                ),
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
            launch {
                delay(5000L)
                if (!SM.lock.value) {
                    for (i in 1..3) {
                        SM.sendHex(hex = V1(fn = "03", pa = "04", data = i.int8ToHex()).toHex())
                        delay(100L)
                    }
                }
            }
            launch {
                SM.callback.collect {
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

    fun pulse(dv: Float, type: Int): Int {
        val me = hpm[type] ?: Motor()
        val ce = hpc[type] ?: 200f
        return me.pulseCount(dv, ce)
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