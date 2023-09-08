package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.core.utils.Constants.MAX_MOTOR
import com.zktony.core.utils.Constants.MAX_TIME
import com.zktony.core.utils.Constants.MAX_VOLTAGE_ZM
import com.zktony.www.R
import com.zktony.www.data.dao.ProgramDao
import com.zktony.www.data.entities.Program
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransferViewModel constructor(
    private val PD: ProgramDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            PD.getAll().collect {
                _uiState.value = _uiState.value.copy(programList = it)
            }
        }
    }

    fun load(id: String) {
        viewModelScope.launch {
            PD.getById(id).collect {
                _uiState.value = _uiState.value.copy(
                    program = it,
                    name = it.name,
                    proteinName = it.proteinName,
                    proteinMax = it.proteinMaxSize,
                    proteinMin = it.proteinMinSize,
                    glueType = it.glueType,
                    glueNormalSize = it.glueConcentration,
                    glueMax = it.glueMaxConcentration,
                    glueMin = it.glueMinConcentration,
                    glueThickness = it.thickness,
                    bufferType = it.bufferType,
                    voltage = it.voltage,
                    motor = it.motor,
                    time = it.time
                )
            }
        }
    }

    fun save(block: () -> Boolean) {
        viewModelScope.launch {
            val programList = _uiState.value.programList
            if (_uiState.value.program == null) {
                if (programList.isNotEmpty() && programList.any { it.name == _uiState.value.name }) {
                    PopTip.show(Ext.ctx.getString(R.string.name_already_exists))
                    return@launch
                }
                PD.insert(
                    Program(
                        name = _uiState.value.name,
                        proteinName = _uiState.value.proteinName,
                        proteinMaxSize = _uiState.value.proteinMax,
                        proteinMinSize = _uiState.value.proteinMin,
                        glueType = _uiState.value.glueType,
                        glueConcentration = _uiState.value.glueNormalSize,
                        glueMaxConcentration = _uiState.value.glueMax,
                        glueMinConcentration = _uiState.value.glueMin,
                        thickness = _uiState.value.glueThickness,
                        bufferType = _uiState.value.bufferType,
                        voltage = _uiState.value.voltage,
                        motor = _uiState.value.motor,
                        time = _uiState.value.time,
                    )
                )
            } else {
                if (programList.isNotEmpty() && programList.any { it.name == _uiState.value.name && it.id != _uiState.value.program!!.id }) {
                    PopTip.show(Ext.ctx.getString(R.string.name_already_exists))
                    return@launch
                }
                PD.update(
                    _uiState.value.program!!.copy(
                        name = _uiState.value.name,
                        proteinName = _uiState.value.proteinName,
                        proteinMaxSize = _uiState.value.proteinMax,
                        proteinMinSize = _uiState.value.proteinMin,
                        glueType = _uiState.value.glueType,
                        glueConcentration = _uiState.value.glueNormalSize,
                        glueMaxConcentration = _uiState.value.glueMax,
                        glueMinConcentration = _uiState.value.glueMin,
                        thickness = _uiState.value.glueThickness,
                        bufferType = _uiState.value.bufferType,
                        voltage = _uiState.value.voltage,
                        motor = _uiState.value.motor,
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

    fun setProteinMin(it: String) {
        _uiState.value = _uiState.value.copy(proteinName = it)
    }

    fun setProteinMin(fl: Float) {
        _uiState.value = _uiState.value.copy(proteinMin = fl)
    }

    fun setProteinMax(fl: Float) {
        _uiState.value = _uiState.value.copy(proteinMax = fl)
    }

    fun setGlueNormalSize(fl: Float) {
        _uiState.value = _uiState.value.copy(glueNormalSize = fl)
    }

    fun setGlueMax(fl: Float) {
        _uiState.value = _uiState.value.copy(glueMax = fl)
    }

    fun setGlueMin(fl: Float) {
        _uiState.value = _uiState.value.copy(glueMin = fl)
    }

    fun setBufferInfo(it: String) {
        _uiState.value = _uiState.value.copy(bufferType = it)
    }

    fun setVoltage(voltage: Float, block: () -> Unit) {
        _uiState.value = _uiState.value.copy(voltage = minOf(voltage, MAX_VOLTAGE_ZM))
        if (voltage > MAX_VOLTAGE_ZM) {
            block()
        }
    }

    fun setTime(time: Float, block: () -> Unit) {
        _uiState.value = _uiState.value.copy(time = minOf(time, MAX_TIME))
        if (time > MAX_TIME) {
            block()
        }
    }

    fun setMotor(motor: Int, block: () -> Unit) {
        _uiState.value = _uiState.value.copy(motor = minOf(motor, MAX_MOTOR))
        if (motor > MAX_MOTOR) {
            block()
        }
    }

    fun setGlueType(i: Int) {
        _uiState.value = _uiState.value.copy(glueType = i)
    }

    fun setGlueThickness(s: String) {
        _uiState.value = _uiState.value.copy(glueThickness = s)
    }
}

data class TransferUiState(
    val programList: List<Program> = emptyList(),
    val program: Program? = null,
    val name: String = "",
    val proteinName: String = "",
    val proteinMin: Float = 0f,
    val proteinMax: Float = 0f,
    val glueType: Int = 0,
    val glueNormalSize: Float = 0f,
    val glueMax: Float = 0f,
    val glueMin: Float = 0f,
    val glueThickness: String = "0.75",
    val bufferType: String = Ext.ctx.getString(R.string.factory),
    val voltage: Float = 0f,
    val motor: Int = 0,
    val time: Float = 0f,
    val save: Boolean = false
)