package com.zktony.www.control.motor

import com.zktony.www.common.room.entity.Calibration
import com.zktony.www.common.room.entity.Motor
import com.zktony.www.common.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.security.cert.X509CRL

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 14:27
 */
class MotorManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private lateinit var x: Motor
    private lateinit var y: Motor
    private lateinit var z: Motor
    private lateinit var p1: Motor
    private lateinit var p2: Motor
    private lateinit var p3: Motor
    private lateinit var p4: Motor
    private lateinit var p5: Motor
    private lateinit var p6: Motor
    private lateinit var cali: Calibration

    fun init(motor: List<Motor>, calibration: List<Calibration>) {
        this.x = motor.find { it.id == 0 } ?: Motor()
        this.y = motor.find { it.id == 1 } ?: Motor()
        this.z = motor.find { it.id == 2 } ?: Motor()
        this.p1 = motor.find { it.id == 3 } ?: Motor()
        this.p2 = motor.find { it.id == 4 } ?: Motor()
        this.p3 = motor.find { it.id == 5 } ?: Motor()
        this.p4 = motor.find { it.id == 6 } ?: Motor()
        this.p5 = motor.find { it.id == 7 } ?: Motor()
        this.p6 = motor.find { it.id == 8 } ?: Motor()
        this.cali = calibration.find { it.enable == 1 } ?: Calibration()
        Logger.d("MotorManager", "init: $cali")
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