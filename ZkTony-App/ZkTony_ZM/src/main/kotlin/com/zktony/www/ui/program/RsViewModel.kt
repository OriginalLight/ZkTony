package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseViewModel
import com.zktony.common.utils.Constants.MAX_TIME
import com.zktony.common.utils.Constants.MAX_VOLTAGE_RS
import com.zktony.www.data.local.room.dao.ProgramDao
import com.zktony.www.data.local.room.entity.Program
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RsViewModel constructor(
    private val dao: ProgramDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(RsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().collect {
                _uiState.value = _uiState.value.copy(programList = it)
            }
        }
    }

    fun load(id: String) {
        viewModelScope.launch {
            dao.getById(id).collect {
                _uiState.value = _uiState.value.copy(
                    program = it,
                    name = it.name,
                    voltage = it.voltage,
                    time = it.time
                )
            }
        }
    }

    fun save(block: () -> Unit) {
        viewModelScope.launch {
            val programList = _uiState.value.programList
            if (_uiState.value.program == null) {
                if (programList.isNotEmpty() && programList.any { it.name == _uiState.value.name }) {
                    PopTip.show("名称已存在")
                    return@launch
                }
                dao.insert(
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
                dao.update(
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

    fun setVoltage(voltage: Float, block: () -> Unit) {
        _uiState.value = _uiState.value.copy(voltage = minOf(voltage, MAX_VOLTAGE_RS))
        if (voltage > MAX_VOLTAGE_RS) {
            block()
        }
    }

    fun setTime(time: Float, block: () -> Unit) {
        _uiState.value = _uiState.value.copy(time = minOf(time, MAX_TIME))
        if (time > MAX_TIME) {
            block()
        }
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