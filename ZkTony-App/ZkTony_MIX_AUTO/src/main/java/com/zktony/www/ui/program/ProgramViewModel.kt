package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.www.room.dao.ProgramDao
import com.zktony.www.room.entity.Program
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ProgramViewModel constructor(
    private val dao: ProgramDao
) : BaseViewModel() {

    private val _programList = MutableStateFlow<List<Program>>(emptyList())
    val workList = _programList.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().collect {
                _programList.value = it
            }
        }
    }

    fun delete(program: Program) {
        viewModelScope.launch {
            dao.delete(program)
        }
    }

    fun insert(name: String, block: (Long) -> Unit) {
        viewModelScope.launch {
            val work = workList.value.find { it.name == name }
            if (work != null) {
                PopTip.show("已存在相同名称的程序")
            } else {
                val program = Program(name = name)
                dao.insert(program)
                block(program.id)
            }
        }
    }
}