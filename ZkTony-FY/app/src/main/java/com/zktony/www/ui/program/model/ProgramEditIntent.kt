package com.zktony.www.ui.program.model

import com.zktony.www.data.entity.Action
import com.zktony.www.model.enum.ActionEnum

/**
 * @author: 刘贺贺
 * @date: 2022-09-30 16:54
 */
sealed class ProgramEditIntent {
    data class OnSwitchAction(val action: ActionEnum) : ProgramEditIntent()
    data class OnDeleteAction(val action: Action) : ProgramEditIntent()
    data class OnLoadActions(val programId: String) : ProgramEditIntent()
    data class OnEditAction(val action: Action) : ProgramEditIntent()
    object OnAddAction : ProgramEditIntent()
}
