package com.zktony.www.manager

import com.zktony.common.utils.logi
import com.zktony.www.data.local.room.dao.CalibrationDao
import com.zktony.www.data.local.room.dao.MotorDao
import com.zktony.www.data.local.room.entity.Calibration
import com.zktony.www.data.local.room.entity.Motor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 14:27
 */
class MotorManager(
    private val motorDao: MotorDao,
    private val calibrationDao: CalibrationDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private var x: Motor = Motor()
    private var y: Motor = Motor()
    private var z: Motor = Motor()
    private var p1: Motor = Motor()
    private var p2: Motor = Motor()
    private var p3: Motor = Motor()
    private var p4: Motor = Motor()
    private var p5: Motor = Motor()
    private var p6: Motor = Motor()
    private var cali: Calibration = Calibration()

    init {
        scope.launch {
            launch {
                motorDao.getAll().collect {
                    if (it.isNotEmpty()) {
                        x = it.find { m -> m.id == 0 } ?: Motor()
                        y = it.find { m -> m.id == 1 } ?: Motor()
                        z = it.find { m -> m.id == 2 } ?: Motor()
                        p1 = it.find { m -> m.id == 3 } ?: Motor()
                        p2 = it.find { m -> m.id == 4 } ?: Motor()
                        p3 = it.find { m -> m.id == 5 } ?: Motor()
                        p4 = it.find { m -> m.id == 6 } ?: Motor()
                        p5 = it.find { m -> m.id == 7 } ?: Motor()
                        p6 = it.find { m -> m.id == 8 } ?: Motor()
                    } else {
                        val motorList = mutableListOf<Motor>()
                        motorList.add(Motor(id = 0, name = "X轴", address = 1))
                        motorList.add(Motor(id = 1, name = "Y轴", address = 2))
                        motorList.add(Motor(id = 2, name = "Z轴", address = 3))
                        for (i in 1..6) {
                            val motor = Motor(
                                id = i + 2,
                                name = "泵$i",
                                address = if (i <= 3) i else i - 3,
                            )
                            motorList.add(motor)
                        }
                        motorDao.insertAll(motorList)
                    }
                }
            }
            launch {
                calibrationDao.getAll().collect {
                    if (it.isNotEmpty()) {
                        cali = it.find { c -> c.enable == 1 } ?: Calibration()
                    } else {
                        calibrationDao.insert(Calibration(enable = 1))
                    }
                }
            }
        }
    }

    fun move(distance: Float, id: Int): Int {
        return when (id) {
            0 -> x.pulseCount(distance, cali.x)
            1 -> y.pulseCount(distance, cali.y)
            2 -> z.pulseCount(distance, cali.z)
            else -> 0
        }
    }

    fun liquid(volume: Float, id: Int): Int {
        return when (id) {
            0 -> p1.pulseCount(volume, cali.v1)
            1 -> p2.pulseCount(volume, cali.v2)
            2 -> p3.pulseCount(volume, cali.v3)
            3 -> p4.pulseCount(volume, cali.v4)
            4 -> p5.pulseCount(volume, cali.v5)
            5 -> p6.pulseCount(volume, cali.v6)
            else -> 0
        }
    }

    fun test() {
        scope.launch { "MotorManager test".logi() }
    }
}