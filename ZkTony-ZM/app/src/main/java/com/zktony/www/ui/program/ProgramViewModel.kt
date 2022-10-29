package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.room.entity.Program
import com.zktony.www.data.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramViewModel @Inject constructor(
    private val programRepository: ProgramRepository
) : BaseViewModel() {

    private val _state = MutableSharedFlow<ProgramState>()
    val state: SharedFlow<ProgramState> get() = _state
    private val intent = MutableSharedFlow<ProgramIntent>()

    init {
        viewModelScope.launch {
            intent.collect {
                when (it) {
                    is ProgramIntent.InsertProgram -> insertProgram(it.program)
                    is ProgramIntent.DeleteProgram -> deleteProgram(it.program)
                    is ProgramIntent.UpdateProgram -> updateProgram(it.program)
                    is ProgramIntent.VerifyProgram -> verifyProgram(it.program)
                }
            }
        }
        viewModelScope.launch {
            programRepository.getAll().collect {
                _state.emit(ProgramState.ChangeProgramList(it))
            }
        }
    }

    fun dispatch(intent: ProgramIntent) {
        try {
            viewModelScope.launch {
                this@ProgramViewModel.intent.emit(intent)
            }
        } catch (_: Exception) {
        }
    }


    private fun insertProgram(program: Program) {
        viewModelScope.launch {
            programRepository.insert(program)
        }
    }

    private fun deleteProgram(program: Program) {
        viewModelScope.launch {
            programRepository.delete(program)
        }
    }

    private fun updateProgram(program: Program) {
        viewModelScope.launch {
            programRepository.update(program)
        }
    }

    /**
     * 判断程序是否可以插入/编辑
     */
    private fun verifyProgram(program: Program) {
        viewModelScope.launch {
            var verify = true
            if (program.name.isEmpty()) {
                verify = false
            }
            if (program.proteinName.isEmpty()) {
                verify = false
            }
            if (program.proteinMinSize == 0f) {
                verify = false
            }
            if (program.proteinMaxSize == 0f) {
                verify = false
            }
            if (program.thickness.isEmpty()) {
                verify = false
            }
            if (program.glueType == 0 && program.glueConcentration == 0f) {
                verify = false
            }
            if (program.glueType == 1) {
                if (program.glueMaxConcentration == 0f || program.glueMinConcentration == 0f) {
                    verify = false
                }
            }
            if (program.bufferType.isEmpty()) {
                verify = false
            }
            if (program.motor == 0 && program.model == 0) {
                verify = false
            }
            if (program.voltage == 0f) {
                verify = false
            }
            if (program.time == 0f) {
                verify = false
            }
            _state.emit(ProgramState.VerifyProgram(verify))
        }
    }

}

sealed class ProgramIntent {
    data class InsertProgram(val program: Program) : ProgramIntent()
    data class UpdateProgram(val program: Program) : ProgramIntent()
    data class DeleteProgram(val program: Program) : ProgramIntent()
    data class VerifyProgram(val program: Program) : ProgramIntent()
}

sealed class ProgramState {
    data class VerifyProgram(val verify: Boolean) : ProgramState()
    data class ChangeProgramList(val programList: List<Program>) : ProgramState()
}