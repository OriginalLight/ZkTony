package com.zktony.www.manager

import com.zktony.core.ext.logi
import com.zktony.www.manager.protocol.V1
import kotlinx.coroutines.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-01 10:28
 */
class ExecutionManager(
    private val SM: SerialManager,
    private val MM: MotorManager,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    // 生成器
    fun builder(
        x: Float = 0f,
        y: Float = 0f,
        v1: Float = 0f,
        v2: Float = 0f,
        v3: Float = 0f,
        v4: Float = 0f
    ): Pair<String, String> {
        val list = MM.pulse(
            listOf(x, y, v1, v2, v3, v4),
            listOf(0, 1, 2, 3, 4, 5),
        )
        return Pair(
            "${list[0]},${list[1]},${list[2]},",
            "${list[3]},${list[4]},${list[5]},"
        )
    }

    // 执行器
    fun actuator(vararg gen: Pair<String, String>) {
        scope.launch {
            val str1 = gen.joinToString("") { it.first }
            val str2 = gen.joinToString("") { it.second }
            SM.sendHex(
                index = 0,
                hex = V1.complex(data = str1),
            )
            SM.sendHex(
                index = 3,
                hex = V1.complex(data = str2),
                lock = true
            )
        }
    }

    fun initializer() {
        "命令执行管理器初始化完成！！！".logi()
    }
}