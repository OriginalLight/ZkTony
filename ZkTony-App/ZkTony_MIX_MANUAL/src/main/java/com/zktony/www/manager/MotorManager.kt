package com.zktony.www.manager

import com.zktony.core.ext.logi
import com.zktony.www.room.dao.CalibrationDao
import com.zktony.www.room.dao.MotorDao
import com.zktony.www.room.entity.Calibration
import com.zktony.www.room.entity.Motor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 14:27
 */
class MotorManager constructor(
    private val motorDao: MotorDao,
    private val calibrationDao: CalibrationDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private var p1: Motor = Motor()
    private var p2: Motor = Motor()
    private var p3: Motor = Motor()
    private var calibration: Calibration = Calibration()

    init {
        scope.launch {
            launch {
                motorDao.getAll().collect {
                    if (it.isNotEmpty()) {
                        p1 = it.find { m -> m.id == 0 } ?: Motor()
                        p2 = it.find { m -> m.id == 1 } ?: Motor()
                        p3 = it.find { m -> m.id == 2 } ?: Motor()
                    } else {
                        motorDao.insertAll(
                            listOf(
                                Motor(id = 0, name = "泵一", address = 1),
                                Motor(id = 1, name = "泵二", address = 2),
                                Motor(id = 2, name = "泵三", address = 3),
                            )
                        )
                    }
                }
            }
            launch {
                calibrationDao.getAll().collect {
                    if (it.isNotEmpty()) {
                        calibration = it.find { c -> c.enable == 1 } ?: Calibration()
                    } else {
                        calibrationDao.insert(Calibration(enable = 1))
                    }
                }
            }
        }
    }

    fun liquid(volume: Float, id: Int): Int {
        return when (id) {
            0 -> p1.pulseCount(volume, calibration.v1)
            1 -> p2.pulseCount(volume, calibration.v2)
            2 -> p3.pulseCount(volume, calibration.v3)
            else -> 0
        }
    }

    fun test() {
        scope.launch { "MotorManager test".logi() }
    }
}