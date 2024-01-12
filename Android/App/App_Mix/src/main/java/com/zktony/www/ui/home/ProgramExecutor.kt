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
            waitLock {
                execute {
                    mode(mode)
                    dv {
                        v1 = colloid / 2f
                        v2 = colloid / 2f
                        v3 = coagulant
                    }
                }
            }
            delay(100L)
            waitAsyncHex {
                pa = "10"
            }
            delay(200L)
            syncHex {
                pa = "0B"
                data = "0305"
            }
            delay(100L)
            waitLock {
                delay(3000L)
            }
            finish()
        }
    }

    suspend fun executePrevious() {
        scope.launch {
            waitLock {
                execute {
                    type(0)
                    mode(mode)
                    dv {
                        v1 = if (mode) 0f else colloid
                        v2 = if (mode) colloid else 0f
                        v3 = coagulant
                    }
                }
            }
            delay(100L)
            waitAsyncHex {
                pa = "10"
            }
            delay(200L)
            syncHex {
                pa = "0B"
                data = "0305"
            }
            delay(100L)
            waitLock {
                delay(3000L)
            }
            finish()
        }
    }
}