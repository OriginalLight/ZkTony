package com.zktony.www.ui

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.www.data.dao.ProgramDao
import com.zktony.www.data.entities.LogRecord
import com.zktony.www.data.entities.Program
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProgramViewModel constructor(
    private val PD: ProgramDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ProgramUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            PD.getAll().collect {
                _uiState.value = _uiState.value.copy(list = it)
            }
        }
    }

    /**
     * 删除程序
     * @param program [Program] 程序
     */
    fun delete(program: Program) {
        viewModelScope.launch {
            PD.delete(program)
        }
    }

    fun select(program: Program?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selected = program)
        }
    }

    fun insert(program: Program) {
        viewModelScope.launch {
            PD.insert(program)
        }
    }

    fun update(program: Program) {
        viewModelScope.launch {
            PD.update(program)
        }
    }

}

data class ProgramUiState(
    val list: List<Program> = emptyList(),
    val selected: Program? = null,
)