package com.zktony.www.ui.home

import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import kotlinx.coroutines.*
import org.koin.java.KoinJavaComponent.inject

/**
 * @author: 刘贺贺
 * @date: 2023-03-21 16:20
 * 任务执行器
 * @param scope 协程作用域
 */
class ProgramExecutor constructor(
    private val colloid: Int,
    private val coagulant: Int,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    var finish: () -> Unit = {}
    private val ex: ExecutionManager by inject(ExecutionManager::class.java)
    private val serial: SerialManager by inject(SerialManager::class.java)


    suspend fun execute() {
        scope.launch {
            while (serial.lock.value) {
                delay(100L)
            }
            ex.actuator(
                ex.builder(
                    v1 = colloid / 2f,
                    v2 = colloid / 2f,
                    v3 = coagulant.toFloat(),
                ),
                type = 1
            )
            delay(100L)
            while (serial.lock.value) {
                delay(100L)
            }
            serial.reset()
            finish()
        }
    }

    suspend fun executePrevious() {
        scope.launch {
            while (serial.lock.value) {
                delay(100L)
            }
            ex.actuator(
                ex.builder(
                    v1 = colloid.toFloat(),
                    v3 = coagulant.toFloat(),
                )

            )
            delay(100L)
            while (serial.lock.value) {
                delay(100L)
            }
            serial.reset()
            finish()
        }
    }
}