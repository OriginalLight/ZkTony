package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.room.entity.Program
import com.zktony.www.common.utils.Constants.MAX_MOTOR
import com.zktony.www.common.utils.Constants.MAX_TIME
import com.zktony.www.common.utils.Constants.MAX_VOLTAGE_ZM
import com.zktony.www.data.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ZmViewModel @Inject constructor(
    private val repo: ProgramRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ZmUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAll().collect {
                _uiState.value = _uiState.value.copy(programList = it)
            }
        }
    }

    fun loadProgram(id: String, block: (ZmUiState) -> Unit) {
        viewModelScope.launch {
            repo.getById(id).collect {
                _uiState.value = _uiState.value.copy(
                    program = it,
                    name = it.name,
                    danbaiName = it.proteinName,
                    danbaiMax = it.proteinMaxSize,
                    danbaiMin = it.proteinMinSize,
                    jiaoKind = it.glueType,
                    jiaoNormalSize = it.glueConcentration,
                    jiaoMax = it.glueMaxConcentration,
                    jiaoMin = it.glueMinConcentration,
                    jiaoHoudu = it.thickness,
                    waterKind = it.bufferType,
                    voltage = it.voltage,
                    motor = it.motor,
                    time = it.time
                )
                block(_uiState.value)
            }
        }
    }

    fun save(block: () -> Boolean) {
        viewModelScope.launch {
            val programList = _uiState.value.programList
            if (_uiState.value.program == null) {
                if (programList.isNotEmpty() && programList.any { it.name == _uiState.value.name }) {
                    PopTip.show("名称已存在")
                    return@launch
                }
                repo.insert(
                    Program(
                        name = _uiState.value.name,
                        proteinName = _uiState.value.danbaiName,
                        proteinMaxSize = _uiState.value.danbaiMax,
                        proteinMinSize = _uiState.value.danbaiMin,
                        glueType = _uiState.value.jiaoKind,
                        glueConcentration = _uiState.value.jiaoNormalSize,
                        glueMaxConcentration = _uiState.value.jiaoMax,
                        glueMinConcentration = _uiState.value.jiaoMin,
                        thickness = _uiState.value.jiaoHoudu,
                        bufferType = _uiState.value.waterKind,
                        voltage = _uiState.value.voltage,
                        motor = _uiState.value.motor,
                        time = _uiState.value.time,
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
                        proteinName = _uiState.value.danbaiName,
                        proteinMaxSize = _uiState.value.danbaiMax,
                        proteinMinSize = _uiState.value.danbaiMin,
                        glueType = _uiState.value.jiaoKind,
                        glueConcentration = _uiState.value.jiaoNormalSize,
                        glueMaxConcentration = _uiState.value.jiaoMax,
                        glueMinConcentration = _uiState.value.jiaoMin,
                        thickness = _uiState.value.jiaoHoudu,
                        bufferType = _uiState.value.waterKind,
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

    fun setDanbaiName(it: String) {
        _uiState.value = _uiState.value.copy(danbaiName = it)
    }

    fun setDanbaiMin(fl: Float) {
        _uiState.value = _uiState.value.copy(danbaiMin = fl)
    }

    fun setDanbaiMax(fl: Float) {
        _uiState.value = _uiState.value.copy(danbaiMax = fl)
    }

    fun setJiaoNormalSize(fl: Float) {
        _uiState.value = _uiState.value.copy(jiaoNormalSize = fl)
    }

    fun setJiaoMax(fl: Float) {
        _uiState.value = _uiState.value.copy(jiaoMax = fl)
    }

    fun setJiaoMin(fl: Float) {
        _uiState.value = _uiState.value.copy(jiaoMin = fl)
    }

    fun setWaterKind(it: String) {
        _uiState.value = _uiState.value.copy(waterKind = it)
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

    fun setJiaoKind(i: Int) {
        _uiState.value = _uiState.value.copy(jiaoKind = i)
    }

    fun setJiaoHoudu(s: String) {
        _uiState.value = _uiState.value.copy(jiaoHoudu = s)
    }
}

data class ZmUiState(
    val programList: List<Program> = emptyList(),
    val program: Program? = null,
    val name: String = "",
    val danbaiName: String = "",
    val danbaiMin: Float = 0f,
    val danbaiMax: Float = 0f,
    val jiaoKind: Int = 0,
    val jiaoNormalSize: Float = 0f,
    val jiaoMax: Float = 0f,
    val jiaoMin: Float = 0f,
    val jiaoHoudu: String = "0.75",
    val waterKind: String = "厂家",
    val voltage: Float = 0f,
    val motor: Int = 0,
    val time: Float = 0f,
    val save: Boolean = false
)