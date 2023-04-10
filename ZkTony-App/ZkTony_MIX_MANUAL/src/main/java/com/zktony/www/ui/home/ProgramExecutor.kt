package com.zktony.www.ui.home

import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    private val executionManager: ExecutionManager by inject(ExecutionManager::class.java)
    private val serialManager: SerialManager by inject(SerialManager::class.java)


    suspend fun execute() {
        scope.launch {
            while (serialManager.lock.value) {
                delay(100L)
            }
            executionManager.executor(
                executionManager.generator(
                    v1 = colloid.toFloat(),
                    v2 = colloid.toFloat(),
                    v3 = coagulant.toFloat(),
                )
            )
            delay(100L)
            while (serialManager.lock.value) {
                delay(100L)
            }
            serialManager.reset()
            finish()
        }
    }

    suspend fun execute2() {
        scope.launch {
            while (serialManager.lock.value) {
                delay(100L)
            }
            executionManager.executor(
                executionManager.generator(
                    v1 = 1000f,
                )
            )
            delay(100L)
            while (serialManager.lock.value) {
                delay(100L)
            }
            serialManager.reset()
            finish()
        }
    }
}