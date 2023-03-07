package com.zktony.www.ui.home

import com.zktony.common.ext.currentTime
import com.zktony.www.common.app.Settings
import com.zktony.www.common.extension.total
import com.zktony.www.control.motion.MotionManager
import com.zktony.www.control.serial.SerialManager
import com.zktony.www.data.local.room.entity.Hole
import com.zktony.www.data.local.room.entity.WorkPlate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 16:20
 * 任务执行器
 * @param scope 协程作用域
 */
class WorkExecutor constructor(
    private val plateList: List<WorkPlate>,
    private val holeList: List<Hole>,
    private val settings: Settings,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    var event: (ExecutorEvent) -> Unit = {}
    private val motion = MotionManager.instance
    private val serial = SerialManager.instance
    private var complete: Int = 0
    private val currentHoleList: MutableList<Hole> = mutableListOf()

    suspend fun execute() {
        scope.launch {
            delay(100L)
            event(ExecutorEvent.Log("[${currentTime()}]------------- 开始执行任务\n"))
            val total = holeList.total()
            if (total > 0) {
                for (e in 0..3) {
                    event(ExecutorEvent.Liquid(e))
                    plateList.forEach { plate ->
                        event(ExecutorEvent.Plate(plate))
                        event(ExecutorEvent.Log("[${currentTime()}]---- ${e + 1}号液体,${plate.sort + 1}号板开始加液\n"))
                        forEachHole(plate.column, plate.row) { i, j ->
                            val hole =
                                holeList.find { it.x == i && it.y == j && it.plateId == plate.id }
                            if (hole != null) {
                                val volume = when (e) {
                                    0 -> hole.v1
                                    1 -> hole.v2
                                    2 -> hole.v3
                                    3 -> hole.v4
                                    else -> 0f
                                }
                                if (hole.checked && volume > 0f) {
                                    currentHoleList.add(Hole(x = i, y = j, checked = true))
                                    event(ExecutorEvent.HoleList(currentHoleList))
                                    while (serial.lock.value || serial.pause.value) {
                                        delay(100)
                                    }
                                    motion.executor(
                                        motion.generator(
                                            x = hole.xAxis,
                                            y = hole.yAxis + settings.needleSpace * e
                                        ),
                                        motion.generator(
                                            x = hole.xAxis,
                                            y = hole.yAxis + settings.needleSpace * e,
                                            v1 = if (e == 0) hole.v1 else 0f,
                                            v2 = if (e == 1) hole.v2 else 0f,
                                            v3 = if (e == 2) hole.v3 else 0f,
                                            v4 = if (e == 3) hole.v4 else 0f
                                        ),
                                    )
                                    delay(100L)
                                    while (serial.lock.value) {
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
            event(ExecutorEvent.Log("[${currentTime()}]------------- 任务执行完毕"))
        }
    }

    // 遍历孔位
    private suspend fun forEachHole(x: Int, y: Int, block: suspend (Int, Int) -> Unit) {
        for (i in 0 until x) {
            if (i % 2 == 0) {
                for (j in 0 until y) {
                    block(i, j)
                }
            } else {
                for (j in y - 1 downTo 0) {
                    block(i, j)
                }
            }
        }
    }
}

sealed class ExecutorEvent {
    data class Plate(val plate: WorkPlate) : ExecutorEvent()
    data class Liquid(val liquid: Int) : ExecutorEvent()
    data class HoleList(val hole: List<Hole>) : ExecutorEvent()
    data class Progress(val total: Int, val complete: Int) : ExecutorEvent()
    data class Log(val log: String) : ExecutorEvent()
    object Finish : ExecutorEvent()
}