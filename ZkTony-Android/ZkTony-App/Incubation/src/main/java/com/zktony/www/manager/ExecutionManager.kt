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
        y: Float = 0f,
        z: Float = 0f,
        v1: Float = 0f,
        v2: Float = 0f,
        v3: Float = 0f,
        v4: Float = 0f,
        v5: Float = 0f,
        v6: Float = 0f
    ): Triple<String, String, String> {
        val list = MM.pulse(
            listOf(y, z, v1, v2, v3, v4, v5, v6),
            listOf(1, 2, 3, 4, 5, 6, 7, 8)
        )
        return Triple(
            "0,${list[0]},${list[1]},",
            "${list[2]},${list[3]},${list[4]},",
            "${list[5]},${list[6]},${list[7]},"
        )
    }

    // 执行器
    fun actuator(vararg gen: Triple<String, String, String>) {
        scope.launch {
            val str1 = gen.joinToString("") { it.first }
            val str2 = gen.joinToString("") { it.second }
            val str3 = gen.joinToString("") { it.third }
            SM.sendHex(
                index = 0,
                hex = V1.complex(data = str1)
            )
            SM.sendHex(
                index = 1,
                hex = V1.complex(data = str2)
            )
            SM.sendHex(
                index = 2,
                hex = V1.complex(data = str3),
                lock = true
            )
        }
    }

    fun initializer() {
        "命令执行管理器初始化完成！！！".logi()
    }
}