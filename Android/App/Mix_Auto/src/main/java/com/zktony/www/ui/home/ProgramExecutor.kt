package com.zktony.www.ui.home

import com.zktony.www.common.ext.*
import com.zktony.www.room.entity.Point
import kotlinx.coroutines.*

/**
 * @author: 刘贺贺
 * @date: 2023-03-21 16:20
 * 任务执行器
 * @param scope 协程作用域
 */
class ProgramExecutor constructor(
    private val list: List<Point>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    var event: (ExecutorEvent) -> Unit = {}
    var pause: Boolean = false
    private var complete: Int = 0
    private val currentList: MutableList<Pair<Int, Boolean>> = mutableListOf()

    suspend fun execute() {
        scope.launch {
            delay(100L)
            val total = list.total()
            if (total > 0) {
                for (i in list.indices.reversed()) {
                    val point = list.find { it.index == i && it.enable }
                    if (point != null && point.enable) {
                        event(ExecutorEvent.Volume(point.v3 to point.v4))
                        currentList.add(Pair(i, true))
                        event(ExecutorEvent.FinishList(currentList))

                        waitLock {
                            while (pause) {
                                delay(100L)
                            }
                            if (point.v3 > 0 && point.v4 > 0) {
                                execute {
                                    step {
                                        y = point.waste
                                    }
                                    step {
                                        y = point.waste
                                        v1 = point.v4.toFloat()
                                        v3 = point.v3.toFloat()
                                    }
                                }
                                delay(100L)
                                waitLock {
                                    syncHex(3) {
                                        pa = "0B"
                                        data = "0305"
                                    }
                                }
                                delay(100L)
                            }
                        }

                        event(ExecutorEvent.Volume(point.v1 to point.v2))

                        waitLock {
                            while (pause) {
                                delay(100L)
                            }
                            if (point.v1 > 0 && point.v2 > 0) {
                                execute {
                                    step {
                                        y = point.axis
                                    }
                                    step {
                                        y = point.axis
                                        v1 = point.v2 / 2f
                                        v2 = point.v2 / 2f
                                        v3 = point.v1.toFloat()
                                    }
                                }
                                delay(100L)
                                waitLock {
                                    syncHex(3) {
                                        pa = "0B"
                                        data = "0305"
                                    }
                                }
                                delay(100L)
                            }
                        }

                        complete += 1
                        event(ExecutorEvent.Progress(total, complete))
                    }
                }
                waitLock {
                    asyncHex(0) { }
                    asyncHex(3) {
                        pa = "0B"
                        data = "0305"
                    }
                }
            }
            event(ExecutorEvent.Finish)
        }
    }
}

sealed class ExecutorEvent {
    data class Volume(val volume: Pair<Int, Int>) : ExecutorEvent()
    data class FinishList(val list: List<Pair<Int, Boolean>>) : ExecutorEvent()
    data class Progress(val total: Int, val complete: Int) : ExecutorEvent()
    object Finish : ExecutorEvent()
}