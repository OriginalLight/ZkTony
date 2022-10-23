package com.zktony.www.ui.program.model

import com.zktony.www.data.entity.Program

sealed class ProgramState {
    data class VerifyProgram(val verify: Boolean) : ProgramState()
    data class ChangeProgramList(val programList: List<Program>) : ProgramState()
}
