package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.extension.showShortToast
import com.zktony.www.data.entity.Program
import com.zktony.www.data.repository.ActionRepository
import com.zktony.www.data.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramViewModel @Inject constructor(
    private val programRepository: ProgramRepository,
    private val actionRepository: ActionRepository
) : BaseViewModel() {

    private val _state = MutableSharedFlow<ProgramState>()
    val state: SharedFlow<ProgramState> get() = _state
    private val intent = MutableSharedFlow<ProgramIntent>()

    init {
        viewModelScope.launch {
            intent.collect {
                when (it) {
                    is ProgramIntent.OnAddProgram -> onAddProgram(it.programName)
                    is ProgramIntent.OnDeleteProgram -> onDeleteProgram(it.program)
                }
            }
        }
        viewModelScope.launch {
            delay(200L)
            programRepository.getAll().collect {
                _state.emit(ProgramState.OnProgramChange(it))
            }
        }
    }

    /**
     * Intent处理器
     * @param intent [ProgramIntent]
     */
    fun dispatch(intent: ProgramIntent) {
        try {
            viewModelScope.launch {
                this@ProgramViewModel.intent.emit(intent)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * 添加程序
     * @param programName [String] 程序名
     */
    private fun onAddProgram(programName: String) {
        viewModelScope.launch {
            programRepository.getByName(programName).firstOrNull()?.let {
                "已存在相同名称的程序".showShortToast()
                return@launch
            }
            val program = Program()
            program.name = programName
            programRepository.insert(program)
        }
    }

    /**
     * 删除程序
     * @param program [Program] 程序
     */
    private fun onDeleteProgram(program: Program) {
        viewModelScope.launch {
            programRepository.delete(program)
            actionRepository.deleteByProgramId(program.id)
        }
    }

}

sealed class ProgramIntent {
    data class OnDeleteProgram(val program: Program) : ProgramIntent()
    data class OnAddProgram(val programName: String) : ProgramIntent()
}

sealed class ProgramState {
    data class OnProgramChange(val programList: List<Program>) : ProgramState()
}