package com.zktony.www.manager

import com.zktony.core.ext.logi
import com.zktony.www.manager.protocol.V1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-02-01 10:28
 */
class ExecutionManager constructor(
    private val MM: MotorManager,
    private val SM: SerialManager,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    // 生成器
    fun builder(
        v1: Float = 0f,
        v2: Float = 0f,
        v3: Float = 0f,
    ): String {
        val mv1 = MM.pulse(v1, 0)
        val mv2 = MM.pulse(v2, 1)
        val mv3 = MM.pulse(v3, 2)
        return "$mv1,$mv2,$mv3,"
    }

    // 执行器
    fun actuator(vararg gen: String, type: Int = 0) {
        scope.launch {
            val str = gen.joinToString("")
            when (type) {
                0 -> SM.sendHex(
                    hex = V1.mutable(data = str),
                    true
                )

                1 -> SM.sendHex(
                    hex = V1.single(data = str),
                    true
                )
            }
        }
    }

    fun initializer() {
        "命令执行管理器初始化完成！！！".logi()
    }
}