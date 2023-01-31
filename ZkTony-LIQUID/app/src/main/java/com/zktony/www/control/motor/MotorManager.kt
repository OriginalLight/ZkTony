package com.zktony.www.control.motor

import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.room.entity.Motor
import com.zktony.www.common.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 14:27
 */
class MotorManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private lateinit var x: Motor
    private lateinit var y: Motor
    private lateinit var p1: Motor
    private lateinit var p2: Motor
    private lateinit var p3: Motor
    private lateinit var p4: Motor
    private lateinit var cali: Calibration

    fun init(motor: List<Motor>, calibration: List<Calibration>) {
        this.x = motor.find { it.board == 0 && it.address == 1 } ?: Motor()
        this.y = motor.find { it.board == 0 && it.address == 2 } ?: Motor()
        this.p1 = motor.find { it.board == 1 && it.address == 1 } ?: Motor()
        this.p2 = motor.find { it.board == 1 && it.address == 2 } ?: Motor()
        this.p3 = motor.find { it.board == 1 && it.address == 3 } ?: Motor()
        this.p4 = motor.find { it.board == 2 && it.address == 1 } ?: Motor()
        this.cali = calibration.find { it.enable == 1 } ?: Calibration()
        Logger.d("MotorManager", "init: $x, $y, $p1, $p2, $p3, $p4, $cali")
    }

    fun move(distanceX: Float, distanceY: Float): Pair<Int, Int> {
        val mx = x.pulseCount(distanceX, cali.x)
        val my = y.pulseCount(distanceY, cali.y)
        return Pair(mx, my)
    }

    fun liquid(volume: Float, id: Int) : Int {
        return when (id) {
            0 -> p1.pulseCount(volume, cali.v1)
            1 -> p2.pulseCount(volume, cali.v2)
            2 -> p3.pulseCount(volume, cali.v3)
            3 -> p4.pulseCount(volume, cali.v4)
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