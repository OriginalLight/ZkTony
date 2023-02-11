package com.zktony.www.control.motor

import com.zktony.www.common.repository.CalibrationRepository
import com.zktony.www.common.repository.MotorRepository
import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.room.entity.Motor
import com.zktony.www.common.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 14:27
 */
class MotorManager(
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    @Inject lateinit var motorRepository: MotorRepository
    @Inject lateinit var calibrationRepository: CalibrationRepository

    private var x: Motor = Motor()
    private var y: Motor = Motor()
    private var p1: Motor = Motor()
    private var p2: Motor = Motor()
    private var p3: Motor = Motor()
    private var p4: Motor = Motor()
    private var calibration: Calibration = Calibration()

    fun initMotor(motorList: List<Motor>) {
        x = motorList.find { it.id == 0 } ?: Motor()
        y = motorList.find { it.id == 1 } ?: Motor()
        p1 = motorList.find { it.id == 2 } ?: Motor()
        p2 = motorList.find { it.id == 3 } ?: Motor()
        p3 = motorList.find { it.id == 4 } ?: Motor()
        p4 = motorList.find { it.id == 5 } ?: Motor()
        Logger.d(msg = "MotorManager initMotor")
    }

    fun initCalibration(calibrationList: List<Calibration>) {
        calibration = calibrationList.find { it.enable == 1 } ?: Calibration()
        Logger.d(msg = "MotorManager initCalibration")
    }

    fun move(distanceX: Float, distanceY: Float): Pair<Int, Int> {
        val mx = x.pulseCount(distanceX, calibration.x)
        val my = y.pulseCount(distanceY, calibration.y)
        return Pair(mx, my)
    }

    fun liquid(volume: Float, id: Int): Int {
        return when (id) {
            0 -> p1.pulseCount(volume, calibration.v1)
            1 -> p2.pulseCount(volume, calibration.v2)
            2 -> p3.pulseCount(volume, calibration.v3)
            3 -> p4.pulseCount(volume, calibration.v4)
            else -> 0
        }
    }


    companion object {
        @JvmStatic
        val instance: MotorManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MotorManager()
        }
    }
}