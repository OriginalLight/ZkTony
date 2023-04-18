package com.zktony.www.manager

import com.zktony.core.ext.int8ToHex
import com.zktony.core.ext.logi
import com.zktony.www.common.ext.toV1
import com.zktony.www.common.ext.toMotor
import com.zktony.www.manager.protocol.V1
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
    private val SM: SerialManager,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     *  0: X轴 1: Y轴 2: 泵1 3: 泵2 4: 泵3 5: 泵4
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
                                Motor(id = 0, name = "X轴", address = 1),
                                Motor(id = 1, name = "Y轴", address = 2),
                                Motor(id = 2, name = "泵一", address = 3),
                                Motor(id = 3, name = "泵二", address = 1),
                                Motor(id = 4, name = "泵三", address = 2),
                                Motor(id = 5, name = "泵四", address = 3)
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
                            hpc[3] = c.v2
                            hpc[4] = c.v3
                            hpc[5] = c.v4
                        }
                    } else {
                        CD.insert(Calibration(enable = 1))
                    }
                }
            }
            launch {
                delay(5000L)
                if (!SM.lock.value) {
                    for (i in 0..1) {
                        for (j in 1..3) {
                            val serial = when (i) {
                                0 -> 0
                                else -> 3
                            }
                            SM.sendHex(
                                index = serial,
                                hex = V1(fn = "03", pa = "04", data = j.int8ToHex()).toHex()
                            )
                            delay(100L)
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
                SM.ttys3Flow.collect {
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