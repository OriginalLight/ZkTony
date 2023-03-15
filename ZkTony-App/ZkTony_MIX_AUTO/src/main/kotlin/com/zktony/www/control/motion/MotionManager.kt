package com.zktony.www.control.motion

import com.zktony.serialport.util.Serial
import com.zktony.www.control.motor.MotorManager
import com.zktony.www.control.serial.SerialManager
import com.zktony.www.control.serial.protocol.V1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-02-01 10:28
 */
class MotionManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val serial = SerialManager.instance
    private val motor = MotorManager.instance

    // 生成器
    fun generator(
        x: Float = 0f,
        z: Float = 0f,
        v1: Float = 0f,
        v2: Float = 0f,
        v3: Float = 0f,
    ): Pair<String, String> {
        val (mx, mz) = motor.move(x, z)
        val mv1 = motor.liquid(v1, 0)
        val mv2 = motor.liquid(v2, 1)
        val mv3 = motor.liquid(v3, 2)
        return Pair(
            "$mx,0,$mz,",
            "$mv1,$mv2,$mv3,"
        )
    }

    // 执行器
    fun executor(vararg gen: Pair<String, String>) {
        scope.launch {
            val str1 = gen.joinToString("") { it.first }
            val str2 = gen.joinToString("") { it.second }
            serial.sendHex(
                serial = Serial.TTYS0,
                hex = V1.complex(data = str1),
            )
            serial.sendHex(
                serial = Serial.TTYS3,
                hex = V1.complex(data = str2),
                lock = true
            )
        }
    }

    companion object {
        @JvmStatic
        val instance: MotionManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MotionManager()
        }
    }
}