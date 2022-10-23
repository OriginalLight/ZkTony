package com.zktony.www.model.state

import com.zktony.www.data.entity.Action
import com.zktony.www.model.enum.ModuleEnum

/**
 * @author: 刘贺贺
 * @date: 2022-10-19 9:25
 */
sealed class ActionState {
    data class Start(val module: ModuleEnum) : ActionState()
    data class CurrentAction(val module: ModuleEnum, val action: Action) : ActionState()
    data class CurrentActionTime(val module: ModuleEnum, val time: String) : ActionState()
    data class Finish(val module: ModuleEnum) : ActionState()
    data class Stop(val module: ModuleEnum) : ActionState()
}