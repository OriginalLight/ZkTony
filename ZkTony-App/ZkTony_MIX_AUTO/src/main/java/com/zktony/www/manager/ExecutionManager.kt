package com.zktony.www.manager

import com.zktony.core.ext.logi
import com.zktony.www.manager.protocol.V1
import kotlinx.coroutines.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-01 10:28
 */
class ExecutionManager constructor(
    private val serialManager: SerialManager,
    private val motorManager: MotorManager,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    fun init() {
        scope.launch {
            "命令执行管理器初始化完成！！！".logi()
        }
    }

    // 生成器
    fun generator(
        y: Float = 0f,
        v1: Float = 0f,
        v2: Float = 0f,
        v3: Float = 0f,
    ): Pair<String, String> {
        val my = motorManager.move(y)
        val mv1 = motorManager.liquid(v1, 0)
        val mv2 = motorManager.liquid(v2, 1)
        val mv3 = motorManager.liquid(v3, 2)
        return Pair(
            "0,$my,0,0,",
            "$mv1,$mv2,$mv3,"
        )
    }

    // 执行器
    fun executor(vararg gen: Pair<String, String>) {
        scope.launch {
            val str1 = gen.joinToString("") { it.first }
            val str2 = gen.joinToString("") { it.second }
            serialManager.sendHex(
                index = 0,
                hex = V1.complex(data = str1),
            )
            serialManager.sendHex(
                index = 3,
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
                index = 0,
                hex = V1.complex(data = str1),
            )
            serialManager.sendHex(
                index = 3,
                hex = V1.complex(data = str2),
                lock = true
            )
        }
    }
}