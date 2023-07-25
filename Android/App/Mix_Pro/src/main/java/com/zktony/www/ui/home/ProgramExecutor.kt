package com.zktony.www.ui.home

import com.zktony.core.ext.loge
import com.zktony.www.core.ext.syncHex
import com.zktony.www.core.ext.tx
import com.zktony.www.core.ext.waitAsyncHex
import com.zktony.www.core.ext.waitLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.nio.file.Files.move

/**
 * @author: 刘贺贺
 * @date: 2023-03-21 16:20
 * 任务执行器
 * @param scope 协程作用域
 */
class ProgramExecutor constructor(
    private val params: List<Float>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    suspend fun execute() {
        scope.launch {
            params.toString().loge()
            tx {
                move {
                    z = 0f
                    y = 0f
                }
            }
            delay(100L)
            waitLock {
                tx {
                    pre {
                        v1 = params[2]
                        v3 = params[3]
                    }
                }
            }
            delay(100L)
            waitLock {
                tx {
                    move {
                        y = params[4]
                    }
                    move {
                        z = params[5]
                        y = params[4]
                    }
                }
            }
            delay(100L)
            waitLock {
                tx {
                    glue {
                        v2 = params[0] / 2f
                        v1 = params[0] / 2f
                        v3 = params[1]
                    }
                }
            }
            delay(100L)
            waitAsyncHex(1) {
                pa = "10"
            }
            delay(200L)
            syncHex(1) {
                pa = "0B"
                data = "0305"
            }
            delay(100L)
            waitLock {
                tx {
                    move {
                        z = 0f
                        y = 0f
                    }
                }
            }
            waitLock {
                delay(3000L)
            }
        }
    }
}