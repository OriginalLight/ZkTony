package com.zktony.www.manager

import com.zktony.common.utils.logi
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
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private var x: Motor = Motor()
    private var z: Motor = Motor()
    private var p1: Motor = Motor()
    private var p2: Motor = Motor()
    private var p3: Motor = Motor()
    private var calibration: Calibration = Calibration()

    fun initMotor(motorList: List<Motor>) {
        scope.launch {
            x = motorList.find { it.id == 0 } ?: Motor()
            z = motorList.find { it.id == 1 } ?: Motor()
            p1 = motorList.find { it.id == 2 } ?: Motor()
            p2 = motorList.find { it.id == 3 } ?: Motor()
            p3 = motorList.find { it.id == 4 } ?: Motor()
            "init motor x:$x z:$z p1:$p1 p2:$p2 p3:$p3".logi()
        }

    }

    fun initCalibration(calibrationList: List<Calibration>) {
        scope.launch {
            calibration = calibrationList.find { it.enable == 1 } ?: Calibration()
            "init calibration:$calibration".logi()
        }
    }

    fun move(distance: Float, id: Int): Int {
        return when (id) {
            0 -> x.pulseCount(distance, calibration.x)
            1 -> z.pulseCount(distance, calibration.z)
            else -> 0
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


    companion object {
        @JvmStatic
        val instance: MotorManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MotorManager()
        }
    }
}