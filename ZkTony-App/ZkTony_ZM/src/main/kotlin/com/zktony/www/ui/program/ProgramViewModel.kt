package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.zktony.common.base.BaseViewModel
import com.zktony.www.data.local.room.dao.ProgramDao
import com.zktony.www.data.local.room.entity.Program
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramViewModel @Inject constructor(
    private val programDao: ProgramDao
) : BaseViewModel() {

    private val _programList = MutableStateFlow(emptyList<Program>())
    val programList = _programList.asStateFlow()

    init {
        viewModelScope.launch {
            programDao.getAll().collect {
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
            programDao.delete(program)
        }
    }

}