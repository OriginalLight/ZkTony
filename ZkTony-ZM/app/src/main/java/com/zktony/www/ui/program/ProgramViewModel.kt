package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.room.entity.Program
import com.zktony.www.data.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramViewModel @Inject constructor(
    private val programRepository: ProgramRepository
) : BaseViewModel() {

    private val _event = MutableSharedFlow<ProgramEvent>()
    val event = _event.asSharedFlow()

    init {
        viewModelScope.launch {
            programRepository.getAll().collect {
                _event.emit(ProgramEvent.ChangeProgramList(it))
            }
        }
    }

    /**
     * 添加程序
     * @param program [Program] 程序
     */
    fun insertProgram(program: Program) {
        viewModelScope.launch {
            programRepository.insert(program)
        }
    }

    /**
     * 删除程序
     * @param program [Program] 程序
     */
    fun deleteProgram(program: Program) {
        viewModelScope.launch {
            programRepository.delete(program)
        }
    }

    /**
     * 更新程序
     * @param program [Program] 程序
     */
    fun updateProgram(program: Program) {
        viewModelScope.launch {
            programRepository.update(program)
        }
    }

    /**
     * 判断程序是否可以插入/编辑
     * @param program [Program] 程序
     */
    fun verifyProgram(program: Program) {
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
            _event.emit(ProgramEvent.VerifyProgram(verify))
        }
    }

}

sealed class ProgramEvent {
    data class VerifyProgram(val verify: Boolean) : ProgramEvent()
    data class ChangeProgramList(val programList: List<Program>) : ProgramEvent()
}