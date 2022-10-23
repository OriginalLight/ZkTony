package com.zktony.www.ui.program.model

import com.zktony.www.data.entity.Program

sealed class ProgramIntent {
    data class InsertProgram(val program: Program) : ProgramIntent()
    data class UpdateProgram(val program: Program) : ProgramIntent()
    data class DeleteProgram(val program: Program) : ProgramIntent()
    data class VerifyProgram(val program: Program) : ProgramIntent()
}
