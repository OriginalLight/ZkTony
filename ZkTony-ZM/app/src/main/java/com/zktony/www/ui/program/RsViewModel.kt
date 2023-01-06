package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.www.base.BaseViewModel
import com.zktony.www.data.model.Program
import com.zktony.www.data.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RsViewModel @Inject constructor(
    private val repo: ProgramRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(RsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAll().collect() {
                _uiState.value = _uiState.value.copy(programList = it)
            }
        }
    }

    fun loadProgram(id: String, block: (RsUiState) -> Unit) {
        viewModelScope.launch {
            repo.getById(id).collect {
                _uiState.value = _uiState.value.copy(
                    program = it,
                    name = it.name,
                    voltage = it.voltage,
                    time = it.time
                )
                block(_uiState.value)
            }
        }
    }

    fun save(block: () -> Unit) {
        viewModelScope.launch {
            val programList = _uiState.value.programList
            if(_uiState.value.program == null) {
                if (programList.isNotEmpty() && programList.any { it.name == _uiState.value.name }) {
                    PopTip.show("名称已存在")
                    return@launch
                }
                repo.insert(
                    Program(
                        name = _uiState.value.name,
                        voltage = _uiState.value.voltage,
                        time = _uiState.value.time,
                        model = 1
                    )
                )
            } else {
                if (programList.isNotEmpty() && programList.any { it.name == _uiState.value.name && it.id != _uiState.value.program!!.id }) {
                    PopTip.show("名称已存在")
                    return@launch
                }
                repo.update(
                    _uiState.value.program!!.copy(
                        name = _uiState.value.name,
                        voltage = _uiState.value.voltage,
                        time = _uiState.value.time,
                        upload = 0
                    )
                )
            }
            block()
        }
    }

    fun setName(it: String) {
        _uiState.value = _uiState.value.copy(name = it)
    }

    fun setVoltage(it: Float) {
        _uiState.value = _uiState.value.copy(voltage = it)
    }

    fun setTime(it: Float) {
        _uiState.value = _uiState.value.copy(time = it)
    }
}

data class RsUiState(
    val program: Program? = null,
    val programList: List<Program> = emptyList(),
    val name: String = "",
    val voltage: Float = 0f,
    val time: Float = 0f,
    val save: Boolean = false,
)