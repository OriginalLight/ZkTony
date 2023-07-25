package com.zktony.www.ui.home

import com.zktony.www.core.ext.*
import kotlinx.coroutines.*

/**
 * @author: 刘贺贺
 * @date: 2023-03-21 16:20
 * 任务执行器
 * @param scope 协程作用域
 */
class ProgramExecutor constructor(
    private val colloid: Float,
    private val coagulant: Float,
    private val mode: Boolean,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    var finish: () -> Unit = {}


    suspend fun execute() {
        scope.launch {
            finish()
        }
    }
}