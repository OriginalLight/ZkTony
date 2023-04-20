package com.zktony.www.manager

import com.zktony.core.ext.logi
import com.zktony.www.manager.protocol.V1
import kotlinx.coroutines.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-01 10:28
 */
class ExecutionManager constructor(
    private val SM: SerialManager,
    private val MM: MotorManager,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    // 生成器
    fun builder(
        y: Float = 0f,
        v1: Float = 0f,
        v2: Float = 0f,
        v3: Float = 0f,
    ): Pair<String, String> {
        val my = MM.pulse(y, 0)
        val mv1 = MM.pulse(v1, 1)
        val mv2 = MM.pulse(v2, 2)
        val mv3 = MM.pulse(v3, 3)
        return Pair(
            "0,$my,0,0,",
            "$mv1,$mv2,$mv3,"
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

    fun actuator(gen: Collection<Pair<String, String>>) {
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