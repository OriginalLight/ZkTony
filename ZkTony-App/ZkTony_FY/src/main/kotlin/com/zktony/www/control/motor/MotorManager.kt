package com.zktony.www.control.motor

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
    private var y: Motor = Motor()
    private var z: Motor = Motor()
    private var p1: Motor = Motor()
    private var p2: Motor = Motor()
    private var p3: Motor = Motor()
    private var p4: Motor = Motor()
    private var p5: Motor = Motor()
    private var p6: Motor = Motor()
    private var cali: Calibration = Calibration()

    fun initMotor(motor: List<Motor>) {
        scope.launch {
            x = motor.find { it.id == 0 } ?: Motor()
            y = motor.find { it.id == 1 } ?: Motor()
            z = motor.find { it.id == 2 } ?: Motor()
            p1 = motor.find { it.id == 3 } ?: Motor()
            p2 = motor.find { it.id == 4 } ?: Motor()
            p3 = motor.find { it.id == 5 } ?: Motor()
            p4 = motor.find { it.id == 6 } ?: Motor()
            p5 = motor.find { it.id == 7 } ?: Motor()
            p6 = motor.find { it.id == 8 } ?: Motor()
        }
    }

    fun initCali(calibration: List<Calibration>) {
        scope.launch {
            cali = calibration.find { it.enable == 1 } ?: Calibration()
            cali.toString().logi()
        }
    }

    fun move(distanceY: Float, distanceZ: Float): Pair<Int, Int> {
        val mx = y.pulseCount(distanceY, cali.y)
        val my = z.pulseCount(distanceZ, cali.z)
        return Pair(mx, my)
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

    companion object {
        @JvmStatic
        val instance: MotorManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MotorManager()
        }
    }
}