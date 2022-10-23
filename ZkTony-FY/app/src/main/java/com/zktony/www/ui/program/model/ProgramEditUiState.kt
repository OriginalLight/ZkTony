package com.zktony.www.ui.program.model

import com.zktony.www.data.entity.Action

/**
 * @author: 刘贺贺
 * @date: 2022-10-21 10:56
 */
data class ProgramEditUiState(
    var programId: String = "",
    var action: Action = Action()
)
