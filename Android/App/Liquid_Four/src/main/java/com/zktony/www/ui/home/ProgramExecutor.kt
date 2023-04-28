package com.zktony.www.ui.home

import com.zktony.core.ext.logi
import com.zktony.www.common.ext.*
import com.zktony.www.room.entity.Point
import kotlinx.coroutines.*

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 16:20
 * 任务执行器
 * @param scope 协程作用域
 */
class ProgramExecutor constructor(
    private val list: List<Point>,
    private val space: Float,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    var event: (ExecutorEvent) -> Unit = {}
    var pause: Boolean = false
    private var complete: Int = 0
    private val currentList: MutableList<Triple<Int, Int, Boolean>> = mutableListOf()

    suspend fun execute() {
        scope.launch {
            delay(100L)
            val total = list.total()
            if (total > 0) {
                list.list().forEach { index ->
                    event(ExecutorEvent.CurrentContainer(index))
                    for (e in 0..3) {
                        event(ExecutorEvent.Liquid(e))
                        list.toString().logi()
                        val pointList = list.filter { it.index == index }
                        val xLength = pointList.maxOf { it.x } + 1
                        val yLength = pointList.maxOf { it.y } + 1
                        forEachHole(xLength, yLength) { i, j ->
                            pointList.find { it.x == i && it.y == j }?.let {
                                val liquid = when (e) {
                                    0 -> it.v1
                                    1 -> it.v2
                                    2 -> it.v3
                                    3 -> it.v4
                                    else -> 0
                                }
                                if (it.enable && liquid > 0) {
                                    waitLock {
                                        while (pause) {
                                            delay(100L)
                                        }
                                        execute {
                                            step {
                                                x = it.xAxis + space * e
                                                y = it.yAxis
                                                v1 = if (e == 0) it.v1.toFloat() else 0f
                                                v2 = if (e == 1) it.v2.toFloat() else 0f
                                                v3 = if (e == 2) it.v3.toFloat() else 0f
                                                v4 = if (e == 3) it.v4.toFloat() else 0f
                                            }
                                        }
                                        delay(100L)
                                        currentList.add(Triple(i, j, true))
                                        event(ExecutorEvent.PointList(currentList))
                                        complete += 1
                                        event(ExecutorEvent.Progress(total, complete))
                                    }
                                }
                            }
                        }
                        waitLock {
                            // 复位
                            asyncHex(0) {}
                        }
                        currentList.clear()
                        event(ExecutorEvent.PointList(currentList))
                    }
                }
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
    data class CurrentContainer(val index: Int) : ExecutorEvent()
    data class Liquid(val liquid: Int) : ExecutorEvent()
    data class PointList(val hole: List<Triple<Int, Int, Boolean>>) : ExecutorEvent()
    data class Progress(val total: Int, val complete: Int) : ExecutorEvent()
    object Finish : ExecutorEvent()
}