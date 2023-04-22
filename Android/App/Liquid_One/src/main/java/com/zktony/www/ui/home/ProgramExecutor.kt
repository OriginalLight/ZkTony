package com.zktony.www.ui.home

import com.zktony.core.ext.Ext
import com.zktony.core.ext.currentTime
import com.zktony.www.common.ext.list
import com.zktony.www.common.ext.total
import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import com.zktony.www.room.entity.Point
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 16:20
 * 任务执行器
 * @param scope 协程作用域
 */
class ProgramExecutor constructor(
    private val list: List<Point>,
    private val settings: Settings,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    var event: (ExecutorEvent) -> Unit = {}
    private val executionManager: ExecutionManager by inject(ExecutionManager::class.java)
    private val serialManager: SerialManager by inject(SerialManager::class.java)
    private var complete: Int = 0
    private val currentList: MutableList<Triple<Int, Int, Boolean>> = mutableListOf()

    suspend fun execute() {
        scope.launch {
            delay(100L)
            event(ExecutorEvent.Log("[ ${currentTime()} ]\t ${Ext.ctx.getString(com.zktony.core.R.string.start)}\n"))
            val total = list.total()
            if (total > 0) {
                list.list().forEach { index ->
                    event(ExecutorEvent.CurrentContainer(index))
                    for (e in 0..3) {
                        event(ExecutorEvent.Liquid(e))
                        event(ExecutorEvent.Log("[ ${currentTime()} ]\t ${e + 1}号液体,${index + 1}号板开始加液\n"))
                        val pointList = list.filter { it.index == index }
                        val x = pointList.maxOf { it.x } + 1
                        val y = pointList.maxOf { it.y } + 1
                        forEachHole(x, y) { i, j ->
                            pointList.find { it.x == i && it.y == j }?.let {
                                val volume = when (e) {
                                    0 -> it.v1
                                    1 -> it.v2
                                    2 -> it.v3
                                    3 -> it.v4
                                    else -> 0
                                }
                                if (it.enable && volume > 0) {
                                    currentList.add(Triple(i, j, true))
                                    event(ExecutorEvent.PointList(currentList))
                                    while (serialManager.lock.value || serialManager.pause.value) {
                                        delay(100)
                                    }
                                    executionManager.actuator(
                                        executionManager.builder(
                                            x = it.xAxis + settings.needleSpace * e,
                                            y = it.yAxis
                                        ),
                                        executionManager.builder(
                                            x = it.xAxis + settings.needleSpace * e,
                                            y = it.yAxis,
                                            v1 = if (e == 0) it.v1.toFloat() else 0f,
                                            v2 = if (e == 1) it.v2.toFloat() else 0f,
                                            v3 = if (e == 2) it.v3.toFloat() else 0f,
                                            v4 = if (e == 3) it.v4.toFloat() else 0f
                                        ),
                                    )
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
                }
            }
            event(ExecutorEvent.Finish)
            event(ExecutorEvent.Log("[ ${currentTime()} ]\t ${Ext.ctx.getString(com.zktony.core.R.string.complete)}"))
        }
    }

    // 遍历孔位
    private suspend fun forEachHole(x: Int, y: Int, block: suspend (Int, Int) -> Unit) {
        for (i in 0 until y) {
            if (i % 2 == 0) {
                for (j in 0 until x) {
                    block(j, i)
                }
            } else {
                for (j in x - 1 downTo 0) {
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
    data class Log(val log: String) : ExecutorEvent()
    object Finish : ExecutorEvent()
}