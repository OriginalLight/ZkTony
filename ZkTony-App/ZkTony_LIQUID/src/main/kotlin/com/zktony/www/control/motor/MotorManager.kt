package com.zktony.www.control.motor

import android.util.Log
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
    private var y: Motor = Motor()
    private var p1: Motor = Motor()
    private var p2: Motor = Motor()
    private var p3: Motor = Motor()
    private var p4: Motor = Motor()
    private var calibration: Calibration = Calibration()

    fun initMotor(motorList: List<Motor>) {
        scope.launch {
            x = motorList.find { it.id == 0 } ?: Motor()
            y = motorList.find { it.id == 1 } ?: Motor()
            p1 = motorList.find { it.id == 2 } ?: Motor()
            p2 = motorList.find { it.id == 3 } ?: Motor()
            p3 = motorList.find { it.id == 4 } ?: Motor()
            p4 = motorList.find { it.id == 5 } ?: Motor()
            Log.d("MotorManager","initMotor")
        }

    }

    fun initCalibration(calibrationList: List<Calibration>) {
        scope.launch {
            calibration = calibrationList.find { it.enable == 1 } ?: Calibration()
            Log.d("MotorManager","initCalibration")
        }
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