package com.zktony.www.ui.home

import com.zktony.common.ext.currentTime
import com.zktony.www.common.ext.total
import com.zktony.www.data.local.room.entity.Container
import com.zktony.www.data.local.room.entity.Hole
import com.zktony.www.data.local.room.entity.Plate
import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    private val ex = ExecutionManager.instance
    private val serial = SerialManager.instance
    private var complete: Int = 0
    private val currentHoleList: MutableList<Pair<Int, Boolean>> = mutableListOf()

    suspend fun execute() {
        scope.launch {
            delay(100L)
            event(ExecutorEvent.Log("[ ${currentTime()} ]\t 开始执行任务\n"))
            val total = holeList.total()
            if (total > 0) {
                val plate = plateList[0]
                for (i in 0 until plate.x) {
                    while (serial.lock.value || serial.pause.value) {
                        delay(100L)
                    }
                    val hole = holeList.find { it.x == i && it.enable }
                    if (hole != null && hole.v1 > 0f && hole.v2 > 0f) {
                        event(ExecutorEvent.CurrentHole(hole))
                        currentHoleList.add(Pair(i, true))
                        event(ExecutorEvent.HoleList(currentHoleList))
                        event(ExecutorEvent.Log("[ ${currentTime()} ]\t 执行孔位：${hole.x} 号孔\n"))
                        ex.executor(
                            ex.generator(
                                x = hole.xAxis,
                                p = hole.v1,
                            ),
                            ex.generator(
                                x = hole.xAxis,
                                z = container.top,
                                p = hole.v1,
                                v1 = hole.v1,
                                v2 = hole.v1,
                                v3 = hole.v2,
                            ),
                            ex.generator(
                                x = hole.xAxis,
                                z = container.bottom,
                                p = hole.v1,
                                v3 = -hole.v2,
                            ),
                        )
                        delay(500L)
                        while (serial.lock.value) {
                            delay(1000)
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