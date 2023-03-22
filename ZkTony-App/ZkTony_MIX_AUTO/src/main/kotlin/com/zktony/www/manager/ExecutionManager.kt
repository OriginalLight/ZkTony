package com.zktony.www.manager

import com.zktony.common.utils.logi
import com.zktony.serialport.util.Serial
import com.zktony.www.manager.protocol.V1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-02-01 10:28
 */
class ExecutionManager constructor(
    private val serialManager: SerialManager,
    private val motorManager: MotorManager,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    // 生成器
    fun generator(
        x: Float = 0f,
        z: Float = 0f,
        p: Float = 0f,
        v1: Float = 0f,
        v2: Float = 0f,
        v3: Float = 0f,
    ): Pair<String, String> {
        val mx = motorManager.move(x, 0)
        val mz = motorManager.move(z, 1)
        val mp = motorManager.liquid(p, 2)
        val mv1 = motorManager.liquid(v1, 0)
        val mv2 = motorManager.liquid(v2, 1)
        val mv3 = motorManager.liquid(v3, 2)
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
            serialManager.sendHex(
                serial = Serial.TTYS0,
                hex = V1.complex(data = str1),
            )
            serialManager.sendHex(
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
            serialManager.sendHex(
                serial = Serial.TTYS0,
                hex = V1.complex(data = str1),
            )
            serialManager.sendHex(
                serial = Serial.TTYS3,
                hex = V1.complex(data = str2),
                lock = true
            )
        }
    }

    fun test() {
        scope.launch { "ExecutionManager test".logi() }
    }
}