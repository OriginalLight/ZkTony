package com.zktony.www.core

import com.zktony.core.ext.logi
import com.zktony.serialport.ext.intToHex
import com.zktony.serialport.protocol.toV1
import com.zktony.www.core.ext.asyncHex
import com.zktony.www.core.ext.collectHex
import com.zktony.www.core.ext.serialPort
import com.zktony.www.data.dao.CalibrationDao
import com.zktony.www.data.dao.MotorDao
import com.zktony.www.data.entities.Motor
import com.zktony.www.data.entities.toMotor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 14:27
 */
class ScheduleTask constructor(
    private val MD: MotorDao,
    private val CD: CalibrationDao,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     *  0: 泵1 1: 泵2 2: 泵3
     */
    val hpc: MutableMap<Int, Double> = ConcurrentHashMap()

    init {
        scope.launch {
            launch {
                MD.getAll().collect {
                    if (it.isEmpty()) {
                        MD.insertAll(
                            listOf(
                                Motor(
                                    id = 0,
                                    name = "排液泵",
                                    address = 1
                                ),
                                Motor(
                                    id = 1,
                                    name = "混合器泵",
                                    address = 2
                                ),
                                Motor(
                                    id = 2,
                                    name = "标准泵",
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
                        val c = it.find { c -> c.active == 1 }
                        if (c != null) {
                            val list = c.avgRate()
                            hpc.clear()
                            for (i in 0..2) {
                                hpc[i] = list[i]
                            }
                        } else {
                            hpc.clear()
                            for (i in 0..2) {
                                hpc[i] = 0.01
                            }
                        }
                    } else {
                        hpc.clear()
                        for (i in 0..2) {
                            hpc[i] = 0.01
                        }
                    }
                }
            }
            launch {
                delay(100L)
                if (serialPort.lock.value.not()) {
                    for (i in 1..3) {
                        asyncHex {
                            fn = "03"
                            pa = "04"
                            data = i.intToHex()
                        }
                        delay(100L)
                    }
                }
            }
            launch {
                collectHex {
                    if (it != null) {
                        val v1 = it.toV1()
                        if (v1 != null && v1.fn == "03" && v1.pa == "04") {
                            val motor = v1.data.toMotor()
                            scope.launch {
                                MD.getById(motor.address - 1).firstOrNull()?.let { m1 ->
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

    fun initializer() {
        "ScheduleTask initializer".logi()
    }

}