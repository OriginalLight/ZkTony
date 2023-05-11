package com.zktony.www.ui.home

import com.zktony.www.common.ext.*
import kotlinx.coroutines.*

/**
 * @author: 刘贺贺
 * @date: 2023-03-21 16:20
 * 任务执行器
 * @param scope 协程作用域
 */
class ProgramExecutor constructor(
    private val colloid: Int,
    private val coagulant: Int,
    private val slowFast: Boolean,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    var finish: () -> Unit = {}


    suspend fun execute() {
        scope.launch {
            waitLock {
                execute {
                    step {
                        v1 = if (slowFast) (colloid / 2f + 1f) else (colloid / 2f - 1f)
                        v2 = if (slowFast) (colloid / 2f - 1f) else (colloid / 2f + 1f)
                        v3 = coagulant.toFloat()
                    }
                }
            }
            delay(100L)
            waitSyncHex {
                pa = "0B"
                data = "0305"
            }
            finish()
        }
    }

    suspend fun executePrevious() {
        scope.launch {
            waitLock {
                execute {
                    type(0)
                    step {
                        v1 = if (slowFast) colloid.toFloat() else 0f
                        v2 = if (slowFast) 0f else colloid.toFloat()
                        v3 = coagulant.toFloat()
                    }
                }
            }
            delay(100L)
            waitSyncHex {
                pa = "0B"
                data = "0305"
            }
            finish()
        }
    }
}