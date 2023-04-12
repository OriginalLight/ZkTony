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
    fun builder(
        v1: Float = 0f,
        v2: Float = 0f,
        v3: Float = 0f,
    ): String {
        val mv1 = motorManager.liquid(v1, 0)
        val mv2 = motorManager.liquid(v2, 1)
        val mv3 = motorManager.liquid(v3, 2)
        return "$mv1,$mv2,$mv3,"
    }

    // 执行器
    fun actuator(vararg gen: String, type: Int = 0) {
        scope.launch {
            val str = gen.joinToString("")
            when(type) {
                0 -> serialManager.sendHex(
                    hex = V1.mutable(data = str),
                    true
                )
                1 -> serialManager.sendHex(
                    hex = V1.single(data = str),
                    true
                )
            }
        }
    }
}