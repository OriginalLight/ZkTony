package com.zktony.www.ui.program.model

import com.zktony.www.data.entity.Program

sealed class ProgramIntent {
    data class OnDeleteProgram(val program: Program) : ProgramIntent()
    data class OnAddProgram(val programName: String) : ProgramIntent()
}
