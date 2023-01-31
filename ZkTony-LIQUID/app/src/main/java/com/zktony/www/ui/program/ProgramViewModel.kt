package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.room.entity.Program
import com.zktony.www.data.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramViewModel @Inject constructor(
    private val programRepo: ProgramRepository,
) : BaseViewModel() {
    private val _programList = MutableStateFlow(emptyList<Program>())
    val programList = _programList.asStateFlow()

    init {
        viewModelScope.launch {
            programRepo.getAll().collect {
                _programList.value = it
            }
        }
    }

    /**
     * 添加程序
     * @param programName [String] 程序名
     */
    fun addProgram(programName: String) {
        viewModelScope.launch {
            programRepo.getByName(programName).firstOrNull()?.let {
                PopTip.show("程序名已存在")
                return@launch
            }
            programRepo.insert(Program(name = programName))
        }
    }

    /**
     * 删除程序
     * @param program [Program] 程序
     */
    fun deleteProgram(program: Program) {
        viewModelScope.launch {
            programRepo.delete(program)
        }
    }
}