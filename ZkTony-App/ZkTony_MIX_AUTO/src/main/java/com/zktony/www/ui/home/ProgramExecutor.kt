package com.zktony.www.ui.home

import com.zktony.core.ext.Ext
import com.zktony.core.ext.currentTime
import com.zktony.www.R
import com.zktony.www.common.ext.total
import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import com.zktony.www.room.entity.Point
import kotlinx.coroutines.*
import org.koin.java.KoinJavaComponent.inject

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
    private val executionManager: ExecutionManager by inject(ExecutionManager::class.java)
    private val serialManager: SerialManager by inject(SerialManager::class.java)
    private var complete: Int = 0
    private val currentList: MutableList<Pair<Int, Boolean>> = mutableListOf()

    suspend fun execute() {
        scope.launch {
            delay(100L)
            event(ExecutorEvent.Log("[ ${currentTime()} ]\t ${Ext.ctx.getString(com.zktony.core.R.string.start)}\n"))
            val total = list.total()
            if (total > 0) {
                for (i in list.indices) {
                    while (serialManager.lock.value || serialManager.pause.value) {
                        delay(100L)
                    }
                    val point = list.find { it.index == 1 && it.enable }
                    if (point != null && point.enable) {
                        event(ExecutorEvent.CurrentPoint(point))

                        if (point.v3 > 0 && point.v4 > 0) {
                            event(
                                ExecutorEvent.Log(
                                    "[ ${currentTime()} ]\t ${point.index + 1} ${
                                        Ext.ctx.getString(
                                            R.string.pre_drainage
                                        )
                                    }\n"
                                )
                            )
                            executionManager.actuator(
                                executionManager.builder(
                                    y = point.waste,
                                    v1 = point.v4.toFloat(),
                                    v3 = point.v3.toFloat(),
                                )
                            )
                            delay(100L)
                            while (serialManager.lock.value) {
                                delay(100L)
                            }
                        }

                        currentList.add(Pair(i, true))
                        event(ExecutorEvent.FinishList(currentList))

                        if (point.v1 > 0 && point.v2 > 0) {
                            event(
                                ExecutorEvent.Log(
                                    "[ ${currentTime()} ]\t ${point.index + 1} ${
                                        Ext.ctx.getString(
                                            R.string.glue_making
                                        )
                                    }\n"
                                )
                            )
                            executionManager.actuator(
                                executionManager.builder(
                                    y = point.axis
                                ),
                                executionManager.builder(
                                    y = point.axis,
                                    v1 = point.v2 / 2f,
                                    v2 = point.v2 / 2f,
                                    v3 = point.v1.toFloat(),
                                ),
                                executionManager.builder(
                                    y = point.axis,
                                    v3 = -point.v1.toFloat(),
                                ),
                            )
                            delay(100L)
                            while (serialManager.lock.value) {
                                delay(100L)
                            }
                        }
                        complete += 1
                        event(ExecutorEvent.Progress(total, complete))
                    }
                }
            }
            event(ExecutorEvent.Finish)
            event(ExecutorEvent.Log("[ ${currentTime()} ]\t ${Ext.ctx.getString(com.zktony.core.R.string.complete)}"))
        }
    }
}

sealed class ExecutorEvent {
    data class CurrentPoint(val point: Point) : ExecutorEvent()
    data class FinishList(val list: List<Pair<Int, Boolean>>) : ExecutorEvent()
    data class Progress(val total: Int, val complete: Int) : ExecutorEvent()
    data class Log(val log: String) : ExecutorEvent()
    object Finish : ExecutorEvent()
}