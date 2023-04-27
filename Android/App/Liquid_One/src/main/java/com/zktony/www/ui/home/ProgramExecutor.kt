package com.zktony.www.ui.home

import com.zktony.www.common.ext.execute
import com.zktony.www.common.ext.total
import com.zktony.www.manager.SerialManager
import com.zktony.www.room.entity.Point
import kotlinx.coroutines.*
import org.koin.java.KoinJavaComponent.inject

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 16:20
 * 任务执行器
 * @param scope 协程作用域
 */
class ProgramExecutor constructor(
    private val list: List<Point>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    var event: (ExecutorEvent) -> Unit = {}
    private val serialManager: SerialManager by inject(SerialManager::class.java)
    private var complete: Int = 0
    private val currentList: MutableList<Triple<Int, Int, Boolean>> = mutableListOf()

    suspend fun execute() {
        scope.launch {
            delay(100L)
            val total = list.total()
            if (total > 0) {
                val xLength = list.maxOf { it.x } + 1
                val yLength = list.maxOf { it.y } + 1
                forEachHole(xLength, yLength) { i, j ->
                    list.find { it.x == i && it.y == j }?.let {
                        if (it.enable && it.v1 > 0) {
                            currentList.add(Triple(i, j, true))
                            event(ExecutorEvent.PointList(currentList))
                            while (serialManager.lock.value || serialManager.pause.value) {
                                delay(100)
                            }
                            execute {
                                step {
                                    x = it.xAxis
                                    y = it.yAxis
                                }
                            }
                            delay(100L)
                            while (serialManager.lock.value || serialManager.pause.value) {
                                delay(100)
                            }
                            execute {
                                step {
                                    x = it.xAxis
                                    y = it.yAxis
                                    v1 = it.v1.toFloat()
                                }
                            }
                            delay(100L)
                            while (serialManager.lock.value) {
                                delay(100)
                            }
                            complete += 1
                            event(ExecutorEvent.Progress(total, complete))
                        }
                    }
                }
                currentList.clear()
                event(ExecutorEvent.PointList(currentList))
            }
            event(ExecutorEvent.Finish)
        }
    }

    // 遍历孔位
    private suspend fun forEachHole(x: Int, y: Int, block: suspend (Int, Int) -> Unit) {
        for (i in 0 until y) {
            if (i % 2 == 0) {
                for (j in x - 1 downTo 0) {
                    block(j, i)
                }
            } else {
                for (j in 0 until x) {
                    block(j, i)
                }
            }
        }
    }
}

sealed class ExecutorEvent {
    data class PointList(val hole: List<Triple<Int, Int, Boolean>>) : ExecutorEvent()
    data class Progress(val total: Int, val complete: Int) : ExecutorEvent()
    object Finish : ExecutorEvent()
}