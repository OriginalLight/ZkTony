package com.zktony.www.ui.admin

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.www.manager.SerialManager
import com.zktony.www.manager.protocol.V1
import com.zktony.www.room.dao.MotorDao
import com.zktony.www.room.entity.Motor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MotorViewModel constructor(
    private val MD: MotorDao,
    private val SM: SerialManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(MotorUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            MD.getAll().distinctUntilChanged().collect {
                _uiState.value = _uiState.value.copy(motorList = it)
                if (it.isNotEmpty() && _uiState.value.motor == null) {
                    _uiState.value = _uiState.value.copy(motor = it[0])
                }
            }
        }
    }

    /**
     * 编辑电机
     * @param motor [Motor]
     */
    fun selectMotor(motor: Motor) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(motor = motor)
        }
    }

    fun model(model: Int) {
        viewModelScope.launch {
            _uiState.value.motor?.let {
                _uiState.value = _uiState.value.copy(motor = it.copy(mode = model))
            }
        }
    }

    fun subdivision(value: Int) {
        viewModelScope.launch {
            _uiState.value.motor?.let {
                _uiState.value = _uiState.value.copy(motor = it.copy(subdivision = value))
            }
        }
    }

    fun speed(i: Int) {
        viewModelScope.launch {
            _uiState.value.motor?.let {
                _uiState.value = _uiState.value.copy(motor = it.copy(speed = i))
            }
        }
    }

    fun acceleration(i: Int) {
        viewModelScope.launch {
            _uiState.value.motor?.let {
                _uiState.value = _uiState.value.copy(motor = it.copy(acceleration = i))
            }
        }
    }

    fun deceleration(i: Int) {
        viewModelScope.launch {
            _uiState.value.motor?.let {
                _uiState.value = _uiState.value.copy(motor = it.copy(deceleration = i))
            }
        }
    }

    fun waitTime(i: Int) {
        viewModelScope.launch {
            _uiState.value.motor?.let {
                _uiState.value = _uiState.value.copy(motor = it.copy(waitTime = i))
            }
        }
    }

    /**
     * 更新电机
     */
    fun update() {
        viewModelScope.launch {
            _uiState.value.motor?.let {
                if (validateMotor(it)) {
                    MD.update(it)
                    val serial = when (it.id) {
                        in 0..2 -> {
                            0
                        }

                        in 3..5 -> {
                            3
                        }

                        else -> {
                            0
                        }
                    }
                    SM.sendHex(
                        index = serial,
                        hex = V1(pa = "04", data = it.toHex()).toHex()
                    )
                    PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.update_success))
                }
            }
        }
    }

    /**
     *  验证电机参数
     *  @param motor [Motor]
     *  @return [Boolean]
     */
    private fun validateMotor(motor: Motor): Boolean {
        if (motor.speed <= 0) {
            viewModelScope.launch {
                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.speed_exception))
            }
            return false
        }
        if (motor.acceleration > 100 || motor.acceleration < 10) {
            viewModelScope.launch {
                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.acceleration_exception))
            }
            return false
        }
        if (motor.deceleration > 100 || motor.deceleration < 10) {
            viewModelScope.launch {
                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.deceleration_exception))
            }
            return false
        }

        return true
    }
}

data class MotorUiState(
    val motorList: List<Motor> = emptyList(),
    val motor: Motor? = null,
)