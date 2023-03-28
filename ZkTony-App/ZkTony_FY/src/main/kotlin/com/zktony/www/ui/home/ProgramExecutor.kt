package com.zktony.www.ui.home

import com.zktony.common.ext.currentTime
import com.zktony.common.ext.getTimeFormat
import com.zktony.common.utils.Queue
import com.zktony.www.data.local.room.entity.Action
import com.zktony.www.data.local.room.entity.ActionEnum
import com.zktony.www.data.local.room.entity.Container
import com.zktony.www.manager.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 16:20
 * 任务执行器
 * @param queue 任务队列
 * @param module 模块
 * @param scope 协程作用域
 */
class ProgramExecutor constructor(
    val queue: Queue<Action>,
    val module: Int,
    val container: Container,
    val settings: Settings,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    var event: (ExecutorEvent) -> Unit = {}
    private val commandExecutor by lazy {
        CommandExecutor(
            module = module,
            con = container,
            event = { event(ExecutorEvent.Wait(module, it)) },
            settings = settings
        )
    }

    /**
     * 执行任务队列
     */
    suspend fun executor() {
        delay(100L)
        queue.dequeue()?.let { action ->
            event(ExecutorEvent.CurrentAction(module, action))
            when (action.mode) {
                ActionEnum.BLOCKING_LIQUID.index -> executeBlockingLiquid(action)
                ActionEnum.ANTIBODY_ONE.index -> executeAntibodyOne(action)
                ActionEnum.ANTIBODY_TWO.index -> executeAntibodyTwo(action)
                ActionEnum.WASHING.index -> executeWashing(action, action.count)
            }
        }
    }

    /**
     * 执行封闭液
     * @param action
     */
    private suspend fun executeBlockingLiquid(action: Action) {
        commandExecutor.run {
            initAction(action)
            addBlockingLiquid {
                event(ExecutorEvent.Log(module, "开始执行封闭液流程"))
                countDown(
                    time = (action.time * 60 * 60).toLong(),
                    onTick = { event(ExecutorEvent.Time(module, it.getTimeFormat())) },
                    onFinish = {
                        event(ExecutorEvent.Log(module, "封闭液流程执行完毕"))
                        event(ExecutorEvent.Log(module, "开始清理封闭液废液"))
                        wasteLiquid {
                            event(ExecutorEvent.Log(module, "清理封闭液废液完毕"))
                            executeNext()
                        }
                    })
            }
        }

    }

    /**
     *  执行一抗
     *  @param action Action
     */
    private suspend fun executeAntibodyOne(action: Action) {
        commandExecutor.run {
            event(ExecutorEvent.Log(module, "开始执行一抗流程"))
            initAction(action)
            addAntibodyOne {
                countDown(
                    time = (action.time * 60 * 60).toLong(),
                    onTick = { event(ExecutorEvent.Time(module, it.getTimeFormat())) },
                    onFinish = {
                        event(ExecutorEvent.Log(module, "一抗流程执行完毕"))
                        event(ExecutorEvent.Log(module, "[ ${currentTime()} ]\t开始回收一抗"))
                        recycleAntibodyOne {
                            event(ExecutorEvent.Log(module, "回收一抗完毕"))
                            executeNext()
                        }
                    })
            }
        }
    }

    /**
     * 执行二抗
     * @param action Action
     */
    private suspend fun executeAntibodyTwo(action: Action) {
        commandExecutor.run {
            event(ExecutorEvent.Log(module, "开始执行二抗流程"))
            initAction(action)
            addAntibodyTwo {
                countDown(
                    time = (action.time * 60 * 60).toLong(),
                    onTick = { event(ExecutorEvent.Time(module, it.getTimeFormat())) },
                    onFinish = {
                        event(ExecutorEvent.Log(module, "二抗流程执行完毕"))
                        event(ExecutorEvent.Log(module, "开始清理二抗废液"))
                        wasteLiquid {
                            event(ExecutorEvent.Log(module, "清理二抗废液完毕"))
                            executeNext()
                        }
                    })
            }
        }
    }

    /**
     * 执行洗涤
     * @param action Action
     */
    private suspend fun executeWashing(action: Action, count: Int) {
        commandExecutor.run {
            event(ExecutorEvent.Count(module, action.count - count + 1))
            event(
                ExecutorEvent.Log(
                    module,
                    "第 ${action.count - count + 1} 次洗涤流程开始执行"
                )
            )
            initAction(action)
            addWashingLiquid {
                countDown(
                    time = (action.time * 60).toLong(),
                    onTick = { event(ExecutorEvent.Time(module, it.getTimeFormat())) },
                    onFinish = {
                        event(
                            ExecutorEvent.Log(
                                module,
                                "第 ${action.count - count + 1} 次洗涤流程执行完毕"
                            )
                        )
                        event(
                            ExecutorEvent.Log(
                                module,
                                "第 ${action.count - count + 1} 次洗涤流程废液清理开始"
                            )
                        )
                        wasteLiquid {
                            event(
                                ExecutorEvent.Log(
                                    module,
                                    "第 ${action.count - count + 1} 次洗涤流程废液清理完毕"
                                )
                            )
                            if (count - 1 > 0) {
                                executeWashing(action, count - 1)
                            } else {
                                executeNext()
                            }
                        }
                    })
            }
        }
    }

    /**
     * 执行下一个任务
     */
    private suspend fun executeNext() {
        if (queue.isEmpty()) {
            event(ExecutorEvent.Finish(module))
        } else {
            executor()
        }
    }

    /**
     * 倒计时
     * @param time 倒计时时间
     * @param onTick 倒计时回调
     * @param onFinish 倒计时结束回调
     */
    private suspend fun countDown(
        time: Long,
        onTick: suspend (Long) -> Unit,
        onFinish: suspend () -> Unit
    ) {
        for (i in time downTo 0) {
            // 每秒执行一次
            onTick.invoke(i)
            // 结束
            if (i == 0L) {
                onFinish.invoke()
            }
            delay(1000L)
        }
    }

}

sealed class ExecutorEvent {
    data class CurrentAction(val module: Int, val action: Action) : ExecutorEvent()
    data class Time(val module: Int, val time: String) : ExecutorEvent()
    data class Log(val module: Int, val msg: String) : ExecutorEvent()
    data class Finish(val module: Int) : ExecutorEvent()
    data class Count(val module: Int, val count: Int) : ExecutorEvent()
    data class Wait(val module: Int, val msg: String) : ExecutorEvent()
}