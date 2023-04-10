package com.zktony.www.ui.home

import com.zktony.common.ext.currentTime
import com.zktony.www.common.ext.total
import com.zktony.www.room.entity.Container
import com.zktony.www.room.entity.Hole
import com.zktony.www.room.entity.Plate
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
    private val container: Container,
    private val plateList: List<Plate>,
    private val holeList: List<Hole>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    var event: (ExecutorEvent) -> Unit = {}
    private val executionManager: ExecutionManager by inject(ExecutionManager::class.java)
    private val serialManager: SerialManager by inject(SerialManager::class.java)
    private var complete: Int = 0
    private val currentHoleList: MutableList<Pair<Int, Boolean>> = mutableListOf()

    suspend fun execute() {
        scope.launch {
            delay(100L)
            event(ExecutorEvent.Log("[ ${currentTime()} ]\t 开始执行任务\n"))
            val total = holeList.total()
            if (total > 0) {
                val plate = plateList[0]
                for (i in 0 until plate.size) {
                    while (serialManager.lock.value || serialManager.pause.value) {
                        delay(100L)
                    }
                    val hole = holeList.find { it.y == i && it.enable }
                    if (hole != null && hole.v1 > 0f && hole.v2 > 0f) {
                        event(ExecutorEvent.CurrentHole(hole))
                        currentHoleList.add(Pair(i, true))
                        event(ExecutorEvent.HoleList(currentHoleList))
                        event(ExecutorEvent.Log("[ ${currentTime()} ]\t 执行孔位：${hole.y} 号孔\n"))
                        executionManager.executor(
                            executionManager.generator(
                                y = hole.yAxis
                            ),
                            executionManager.generator(
                                y = hole.yAxis,
                                v1 = hole.v2,
                                v2 = hole.v2,
                                v3 = hole.v1,
                            ),
                            executionManager.generator(
                                y = hole.yAxis,
                                v3 = -hole.v1,
                            ),
                        )
                        delay(100L)
                        while (serialManager.lock.value) {
                            delay(100L)
                        }
                        event(ExecutorEvent.Log("[ ${currentTime()} ]\t ${hole.y} 号孔排液\n"))
                        executionManager.executor(
                            executionManager.generator(
                                y = hole.yAxis + container.space,
                                v1 = 2000f
                            )
                        )
                        delay(100L)
                        while (serialManager.lock.value) {
                            delay(100L)
                        }
                        complete += 1
                        event(ExecutorEvent.Progress(total, complete))
                    }
                }
            }
            event(ExecutorEvent.Finish)
            event(ExecutorEvent.Log("[ ${currentTime()} ]\t 任务执行完毕"))
        }
    }
}

sealed class ExecutorEvent {
    data class CurrentHole(val hole: Hole) : ExecutorEvent()
    data class HoleList(val hole: List<Pair<Int, Boolean>>) : ExecutorEvent()
    data class Progress(val total: Int, val complete: Int) : ExecutorEvent()
    data class Log(val log: String) : ExecutorEvent()
    object Finish : ExecutorEvent()
}