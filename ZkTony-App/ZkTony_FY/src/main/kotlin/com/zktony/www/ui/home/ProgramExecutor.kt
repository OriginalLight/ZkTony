package com.zktony.www.ui.home

import com.zktony.common.extension.getTimeFormat
import com.zktony.common.utils.Queue
import com.zktony.www.common.app.Settings
import com.zktony.www.data.local.room.entity.Action
import com.zktony.www.data.local.room.entity.ActionEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 16:20
 * 任务执行器
 * @param queue 任务队列
 * @param module 模块
 * @param settings 设置状态
 * @param scope 协程作用域
 */
class ProgramExecutor constructor(
    val queue: Queue<Action>,
    val module: Int,
    val settings: Settings,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _event = MutableSharedFlow<ActionEvent>()
    val event: SharedFlow<ActionEvent> get() = _event
    private val commandExecutor by lazy {
        CommandExecutor(
            module = module,
            con = settings.container
        )
    }

    /**
     * 执行任务队列
     */
    suspend fun run() {
        queue.dequeue()?.let { action ->
            _event.emit(ActionEvent.CurrentAction(module, action))
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
            val listener = scope.launch {
                wait.collect {
                    _event.emit(ActionEvent.Wait(module, it))
                }
            }
            listener.start()
            addBlockingLiquid {
                countDown(
                    time = (action.time * 60 * 60).toLong(),
                    onTick = {
                        _event.emit(ActionEvent.Time(module, it.getTimeFormat()))
                    },
                    onFinish = {
                        wasteLiquid {
                            listener.cancel()
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
            initAction(action)
            val listener = scope.launch {
                wait.collect {
                    _event.emit(ActionEvent.Wait(module, it))
                }
            }
            listener.start()
            addAntibodyOne {
                countDown(
                    time = (action.time * 60 * 60).toLong(),
                    onTick = {
                        _event.emit(ActionEvent.Time(module, it.getTimeFormat()))
                    },
                    onFinish = {
                        recycleAntibodyOne {
                            listener.cancel()
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
            initAction(action)
            val listener = scope.launch {
                wait.collect {
                    _event.emit(ActionEvent.Wait(module, it))
                }
            }
            listener.start()
            addAntibodyTwo {
                countDown(
                    time = (action.time * 60 * 60).toLong(),
                    onTick = {
                        _event.emit(ActionEvent.Time(module, it.getTimeFormat()))
                    }, onFinish = {
                        wasteLiquid {
                            listener.cancel()
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
            initAction(action)
            val listener = scope.launch {
                wait.collect {
                    _event.emit(ActionEvent.Wait(module, it))
                }
            }
            listener.start()
            _event.emit(ActionEvent.Count(module, action.count - count + 1))
            addWashingLiquid {
                countDown(
                    time = (action.time * 60).toLong(),
                    onTick = {
                        _event.emit(ActionEvent.Time(module, it.getTimeFormat()))
                    },
                    onFinish = {
                        wasteLiquid {
                            if (count - 1 > 0) {
                                listener.cancel()
                                executeWashing(action, count - 1)
                            } else {
                                listener.cancel()
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
            // 任务队列执行完成
            _event.emit(ActionEvent.Finish(module))
        } else {
            // 继续执行任务队列
            run()
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

sealed class ActionEvent {
    data class CurrentAction(val module: Int, val action: Action) : ActionEvent()
    data class Time(val module: Int, val time: String) : ActionEvent()
    data class Finish(val module: Int) : ActionEvent()
    data class Count(val module: Int, val count: Int) : ActionEvent()
    data class Wait(val module: Int, val msg: String) : ActionEvent()
}