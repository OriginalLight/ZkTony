package com.zktony.www.ui.program.model

import com.zktony.www.data.entity.Action
import com.zktony.www.model.enum.ActionEnum

/**
 * @author: 刘贺贺
 * @date: 2022-09-30 16:55
 */
sealed class ProgramEditState {
    data class OnSwitchAction(val action: ActionEnum) : ProgramEditState()
    data class OnActionChange(val actionList: List<Action>) : ProgramEditState()
    data class OnButtonChange(val enable: Boolean) : ProgramEditState()
}
