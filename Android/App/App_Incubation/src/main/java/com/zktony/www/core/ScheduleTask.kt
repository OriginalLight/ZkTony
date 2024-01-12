package com.zktony.www.core

import com.zktony.core.ext.Ext
import com.zktony.core.ext.logi
import com.zktony.serialport.ext.intToHex
import com.zktony.serialport.protocol.toV1
import com.zktony.www.R
import com.zktony.www.core.ext.asyncHex
import com.zktony.www.core.ext.collectHex
import com.zktony.www.core.ext.serialPort
import com.zktony.www.data.dao.CalibrationDao
import com.zktony.www.data.dao.ContainerDao
import com.zktony.www.data.dao.MotorDao
import com.zktony.www.data.entities.Container
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
    private val containerDao: ContainerDao,
    private val calibrationDao: CalibrationDao,
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
                motorDao.getAll().collect {
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
                        motorDao.insertAll(list)
                    }
                }
            }
            launch {
                calibrationDao.getAll().collect {
                    if (it.isNotEmpty()) {
                        val c = it.find { c -> c.active == 1 }
                        if (c != null) {
                            hpc.clear()
                            hpc[0] = 0f
                            hpc[1] = 58f / 3200f
                            hpc[2] = 3.8f / 3200f
                            val list = c.vps()
                            list.forEachIndexed { index, fl ->
                                hpc[index + 3] = fl
                            }
                        } else {
                            calibrationDao.update(it[0].copy(active = 1))
                        }
                    } else {
                        hpc.clear()
                        hpc[0] = 0f
                        hpc[1] = 58f / 3200f
                        hpc[2] = 3.8f / 3200f
                        hpc[3] = 150f / 3200f
                        hpc[4] = 150f / 3200f
                        hpc[5] = 150f / 3200f
                        hpc[6] = 150f / 3200f
                        hpc[7] = 150f / 3200f
                        hpc[8] = 150f / 3200f
                    }
                }
            }
            launch {
                delay(5000L)
                if (serialPort.lock.value.not()) {
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
            launch {
                containerDao.getAll().collect {
                    if (it.isEmpty()) {
                        containerDao.insert(Container())
                    }
                }
            }
        }
    }


    fun initializer() {
        "ScheduleTask initializer".logi()
    }
}