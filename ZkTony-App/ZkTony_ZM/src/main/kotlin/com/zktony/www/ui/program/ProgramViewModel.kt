package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.zktony.common.base.BaseViewModel
import com.zktony.www.data.local.room.dao.ProgramDao
import com.zktony.www.data.local.room.entity.Program
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProgramViewModel constructor(
    private val dao: ProgramDao
) : BaseViewModel() {

    private val _programList = MutableStateFlow(emptyList<Program>())
    val programList = _programList.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().collect {
                _programList.value = it
            }
        }
    }

    /**
     * 删除程序
     * @param program [Program] 程序
     */
    fun delete(program: Program) {
        viewModelScope.launch {
            dao.delete(program)
        }
    }

}