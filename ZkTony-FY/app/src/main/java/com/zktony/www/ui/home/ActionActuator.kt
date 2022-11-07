package com.zktony.www.ui.home

import com.zktony.www.common.app.SettingState
import com.zktony.www.common.extension.getTimeFormat
import com.zktony.www.common.model.Queue
import com.zktony.www.common.room.entity.Action
import com.zktony.www.common.room.entity.ActionEnum
import com.zktony.www.common.utils.Logger
import com.zktony.www.serialport.protocol.CommandGroup
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 16:20
 * 任务执行器
 * @param actionQueue 任务队列
 * @param module 模块
 * @param settingState 设置状态
 */
class ActionActuator private constructor(
    private val actionQueue: Queue<Action>,
    private val module: ModuleEnum,
    private val settingState: SettingState,
) {
    private val _state = MutableSharedFlow<ActionEvent>()
    val state: SharedFlow<ActionEvent> get() = _state
    private val commandGroup by lazy { CommandGroup() }

    /**
     * 执行任务队列
     */
    suspend fun run() {
        actionQueue.peek()?.let { action ->
            _state.emit(ActionEvent.CurrentAction(module, action))
            when (action.mode) {
                ActionEnum.BLOCKING_LIQUID.index -> executeBlockingLiquid(action)
                ActionEnum.ANTIBODY_ONE.index -> executeAntibodyOne(action)
                ActionEnum.ANTIBODY_TWO.index -> executeAntibodyTwo(action)
                ActionEnum.WASHING.index -> executeWashing(action, action.count)
                else -> {}
            }
        }
    }

    /**
     * 执行封闭液
     * @param action
     */
    private suspend fun executeBlockingLiquid(action: Action) {
        commandGroup.run {
            initModule(this@ActionActuator.module)
            initSettingState(this@ActionActuator.settingState)
            initAction(action)
            addBlockingLiquid {
                countDown((action.time * 60 * 60).toLong(), {
                    _state.emit(ActionEvent.CurrentActionTime(module, it.getTimeFormat()))
                },
                    {
                        wasteLiquid { executeNext() }
                    })
            }
        }

    }

    /**
     *  执行一抗
     *  @param action Action
     */
    private suspend fun executeAntibodyOne(action: Action) {
        commandGroup.run {
            initModule(this@ActionActuator.module)
            initSettingState(this@ActionActuator.settingState)
            initAction(action)
            addAntibodyOne {
                countDown((action.time * 60 * 60).toLong(), {
                    _state.emit(ActionEvent.CurrentActionTime(module, it.getTimeFormat()))
                },
                    {
                        recycleAntibodyOne { executeNext() }
                    })
            }
        }
    }

    /**
     * 执行二抗
     * @param action Action
     */
    private suspend fun executeAntibodyTwo(action: Action) {
        commandGroup.run {
            initModule(this@ActionActuator.module)
            initSettingState(this@ActionActuator.settingState)
            initAction(action)
            addAntibodyTwo {
                countDown((action.time * 60 * 60).toLong(), {
                    _state.emit(ActionEvent.CurrentActionTime(module, it.getTimeFormat()))
                },
                    {
                        wasteLiquid { executeNext() }
                    })
            }
        }
    }

    /**
     * 执行洗涤
     * @param action Action
     */
    private suspend fun executeWashing(action: Action, count: Int) {
        commandGroup.run {
            initModule(module)
            initSettingState(settingState)
            initAction(action)
            _state.emit(ActionEvent.Count(module, action.count - count + 1))
            addWashingLiquid {
                countDown((action.time * 60).toLong(), {
                    _state.emit(ActionEvent.CurrentActionTime(module, it.getTimeFormat()))
                },
                    {
                        wasteLiquid { }
                        if (count - 1 > 0) {
                            executeWashing(action, count - 1)
                        } else {
                            executeNext()
                        }
                    })
            }
        }
    }

    /**
     * 执行下一个任务
     */
    private suspend fun executeNext() {
        actionQueue.run {
            dequeue()
            if (isEmpty()) {
                // 任务队列执行完成
                Logger.e(msg = "${module.value}任务队列执行完成")
                _state.emit(ActionEvent.Finish(module))
            } else {
                // 继续执行任务队列
                Logger.e(msg = "${module.value}执行下一个任务")
                run()
            }
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

    companion object {
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder {
        private var actionQueue: Queue<Action> = Queue()
        private var module: ModuleEnum = ModuleEnum.A
        private var settingState: SettingState = SettingState()
        fun setActionQueue(actionQueue: Queue<Action>) = apply { this.actionQueue = actionQueue }
        fun setModule(module: ModuleEnum) = apply { this.module = module }
        fun setSettingState(settingState: SettingState) = apply { this.settingState = settingState }

        fun build() = ActionActuator(actionQueue, module, settingState)
    }

}

sealed class ActionEvent {
    data class CurrentAction(val module: ModuleEnum, val action: Action) : ActionEvent()
    data class CurrentActionTime(val module: ModuleEnum, val time: String) : ActionEvent()
    data class Finish(val module: ModuleEnum) : ActionEvent()
    data class Count(val module: ModuleEnum, val count: Int) : ActionEvent()
}