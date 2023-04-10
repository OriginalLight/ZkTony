package com.zktony.www.ui.home

import com.zktony.common.ext.currentTime
import com.zktony.www.common.ext.total
import com.zktony.www.room.entity.Hole
import com.zktony.www.room.entity.Plate
import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.SerialManager
import com.zktony.www.manager.Settings
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
    private val plateList: List<Plate>,
    private val holeList: List<Hole>,
    private val settings: Settings,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    var event: (ExecutorEvent) -> Unit = {}
    private val executionManager: ExecutionManager by inject(ExecutionManager::class.java)
    private val serialManager: SerialManager by inject(SerialManager::class.java)
    private var complete: Int = 0
    private val currentHoleList: MutableList<Triple<Int, Int, Boolean>> = mutableListOf()

    suspend fun execute() {
        scope.launch {
            delay(100L)
            event(ExecutorEvent.Log("[ ${currentTime()} ]\t 开始执行任务\n"))
            val total = holeList.total()
            if (total > 0) {
                for (e in 0..3) {
                    event(ExecutorEvent.Liquid(e))
                    plateList.forEach { plate ->
                        event(ExecutorEvent.CurrentPlate(plate))
                        event(ExecutorEvent.Log("[ ${currentTime()} ]\t ${e + 1}号液体,${plate.index + 1}号板开始加液\n"))
                        forEachHole(plate.x, plate.y) { i, j ->
                            val hole =
                                holeList.find { it.x == i && it.y == j && it.subId == plate.id }
                            if (hole != null) {
                                val volume = when (e) {
                                    0 -> hole.v1
                                    1 -> hole.v2
                                    2 -> hole.v3
                                    3 -> hole.v4
                                    else -> 0f
                                }
                                if (hole.enable && volume > 0f) {
                                    currentHoleList.add(Triple(i, j, true))
                                    event(ExecutorEvent.HoleList(currentHoleList))
                                    while (serialManager.lock.value || serialManager.pause.value) {
                                        delay(100)
                                    }
                                    executionManager.executor(
                                        executionManager.generator(
                                            x = hole.xAxis + settings.needleSpace * e,
                                            y = hole.yAxis
                                        ),
                                        executionManager.generator(
                                            x = hole.xAxis + settings.needleSpace * e,
                                            y = hole.yAxis,
                                            v1 = if (e == 0) hole.v1 else 0f,
                                            v2 = if (e == 1) hole.v2 else 0f,
                                            v3 = if (e == 2) hole.v3 else 0f,
                                            v4 = if (e == 3) hole.v4 else 0f
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
                        currentHoleList.clear()
                        event(ExecutorEvent.HoleList(currentHoleList))
                    }
                }
            }
            event(ExecutorEvent.Finish)
            event(ExecutorEvent.Log("[ ${currentTime()} ]\t 任务执行完毕"))
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
    data class CurrentPlate(val plate: Plate) : ExecutorEvent()
    data class Liquid(val liquid: Int) : ExecutorEvent()
    data class HoleList(val hole: List<Triple<Int, Int, Boolean>>) : ExecutorEvent()
    data class Progress(val total: Int, val complete: Int) : ExecutorEvent()
    data class Log(val log: String) : ExecutorEvent()
    object Finish : ExecutorEvent()
}