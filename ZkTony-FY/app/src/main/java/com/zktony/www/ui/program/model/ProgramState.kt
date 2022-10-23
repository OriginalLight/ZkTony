package com.zktony.www.ui.program.model

import com.zktony.www.data.entity.Program

sealed class ProgramState {
    data class OnProgramChange(val programList: List<Program>) : ProgramState()
}
