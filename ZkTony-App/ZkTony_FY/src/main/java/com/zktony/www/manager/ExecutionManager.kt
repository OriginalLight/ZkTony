package com.zktony.www.manager

import com.zktony.core.ext.logi
import com.zktony.serialport.util.Serial
import com.zktony.www.manager.protocol.V1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-02-01 10:28
 */
class ExecutionManager(
    private val serialManager: SerialManager,
    private val motorManager: MotorManager,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    // 生成器
    fun generator(
        y: Float = 0f,
        z: Float = 0f,
        v1: Float = 0f,
        v2: Float = 0f,
        v3: Float = 0f,
        v4: Float = 0f,
        v5: Float = 0f,
        v6: Float = 0f
    ): Triple<String, String, String> {
        val my = motorManager.move(y, 1)
        val mz = motorManager.move(z, 2)
        val mv1 = motorManager.liquid(v1, 0)
        val mv2 = motorManager.liquid(v2, 1)
        val mv3 = motorManager.liquid(v3, 2)
        val mv4 = motorManager.liquid(v4, 3)
        val mv5 = motorManager.liquid(v5, 4)
        val mv6 = motorManager.liquid(v6, 5)
        return Triple(
            "0,$my,$mz,",
            "$mv1,$mv2,$mv3,",
            "$mv4,$mv5,$mv6,"
        )
    }

    // 执行器
    fun executor(vararg gen: Triple<String, String, String>) {
        scope.launch {
            val str1 = gen.joinToString("") { it.first }
            val str2 = gen.joinToString("") { it.second }
            val str3 = gen.joinToString("") { it.third }
            while (serialManager.lock.value) {
                delay(100L)
            }
            serialManager.sendHex(
                serial = Serial.TTYS0,
                hex = V1.complex(data = str1)
            )
            serialManager.sendHex(
                serial = Serial.TTYS1,
                hex = V1.complex(data = str2)
            )
            serialManager.sendHex(
                serial = Serial.TTYS2,
                hex = V1.complex(data = str3),
                lock = true
            )
        }
    }


    fun executor(gen: Collection<Triple<String, String, String>>) {
        scope.launch {
            val str1 = gen.joinToString("") { it.first }
            val str2 = gen.joinToString("") { it.second }
            val str3 = gen.joinToString("") { it.third }
            while (serialManager.lock.value) {
                delay(100L)
            }
            serialManager.sendHex(
                serial = Serial.TTYS0,
                hex = V1.complex(data = str1)
            )
            serialManager.sendHex(
                serial = Serial.TTYS1,
                hex = V1.complex(data = str2)
            )
            serialManager.sendHex(
                serial = Serial.TTYS2,
                hex = V1.complex(data = str3),
                lock = true
            )
        }
    }

    fun test() {
        scope.launch { "ExecutionManager test".logi() }
    }
}