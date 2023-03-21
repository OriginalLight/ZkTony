package com.zktony.www.manager

import com.zktony.serialport.util.Serial
import com.zktony.www.manager.protocol.V1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-02-01 10:28
 */
class ExecutionManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val sm = SerialManager.instance
    private val mm = MotorManager.instance

    // 生成器
    fun generator(
        x: Float = 0f,
        z: Float = 0f,
        p: Float = 0f,
        v1: Float = 0f,
        v2: Float = 0f,
        v3: Float = 0f,
    ): Pair<String, String> {
        val mx = mm.move(x, 0)
        val mz = mm.move(z, 1)
        val mp = mm.liquid(p, 2)
        val mv1 = mm.liquid(v1, 0)
        val mv2 = mm.liquid(v2, 1)
        val mv3 = mm.liquid(v3, 2)
        return Pair(
            "$mz,$mx,$mp,",
            "$mv1,$mv2,$mv3,"
        )
    }

    // 执行器
    fun executor(vararg gen: Pair<String, String>) {
        scope.launch {
            val str1 = gen.joinToString("") { it.first }
            val str2 = gen.joinToString("") { it.second }
            sm.sendHex(
                serial = Serial.TTYS0,
                hex = V1.complex(data = str1),
            )
            sm.sendHex(
                serial = Serial.TTYS3,
                hex = V1.complex(data = str2),
                lock = true
            )
        }
    }

    fun executor(gen: Collection<Pair<String, String>>) {
        scope.launch {
            val str1 = gen.joinToString("") { it.first }
            val str2 = gen.joinToString("") { it.second }
            sm.sendHex(
                serial = Serial.TTYS0,
                hex = V1.complex(data = str1),
            )
            sm.sendHex(
                serial = Serial.TTYS3,
                hex = V1.complex(data = str2),
                lock = true
            )
        }
    }

    companion object {
        @JvmStatic
        val instance: ExecutionManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ExecutionManager()
        }
    }
}