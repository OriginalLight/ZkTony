package com.zktony.www.ui.admin

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.room.entity.Motor
import com.zktony.www.data.repository.MotorRepository
import com.zktony.www.serialport.getSerialPortEnum
import com.zktony.www.serialport.protocol.Command
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotorSettingViewModel @Inject constructor(
    private val motorRepository: MotorRepository
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val _state = MutableSharedFlow<MotorSettingState>()
    val state: SharedFlow<MotorSettingState> get() = _state

    private val _uiState = MutableStateFlow(MotorSettingUiState())
    val uiState: StateFlow<MotorSettingUiState> get() = _uiState

    /**
     * 初始化电机
     */
    fun initMotors() {
        viewModelScope.launch {
            motorRepository.getAll().collect { motors ->
                _uiState.value =
                    _uiState.value.copy(motor = if (_uiState.value.motor.name.isEmpty()) motors.first() else _uiState.value.motor)
                _state.emit(MotorSettingState.OnMotorValueChange(uiState.value.motor))
                _state.emit(MotorSettingState.OnDataBaseChange(motors))
            }
        }
    }

    /**
     * 电机修改参数
     * @param motor [Motor]
     */
    fun motorValueChange(motor: Motor) {
        _uiState.value = _uiState.value.copy(motor = motor)
    }

    /**
     * 编辑电机
     * @param motor [Motor]
     */
    fun editMotor(motor: Motor) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(motor = motor)
            _state.emit(MotorSettingState.OnMotorValueChange(motor))
        }
    }

    /**
     * 更新电机
     */
    fun updateMotor() {
        viewModelScope.launch {
            val motor = uiState.value.motor
            if (validateMotor(motor)) {
                motorRepository.update(motor)
                appViewModel.sender(
                    getSerialPortEnum(motor.board),
                    Command(
                        parameter = "04",
                        data = motor.toHex()
                    ).toHex()
                )
                _state.emit(MotorSettingState.OnUpdateMessage("更新成功"))
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
                _state.emit(MotorSettingState.OnUpdateMessage("速度不能小于0"))
            }
            return false
        }
        if (motor.acceleration > 100 || motor.acceleration < 10) {
            viewModelScope.launch {
                _state.emit(MotorSettingState.OnUpdateMessage("加速度范围10-100"))
            }
            return false
        }
        if (motor.deceleration > 100 || motor.deceleration < 10) {
            viewModelScope.launch {
                _state.emit(MotorSettingState.OnUpdateMessage("减速度范围10-100"))
            }
            return false
        }

        return true
    }
}


sealed class MotorSettingState {
    data class OnDataBaseChange(val motorList: List<Motor>) : MotorSettingState()
    data class OnUpdateMessage(val message: String) : MotorSettingState()
    data class OnMotorValueChange(val motor: Motor) : MotorSettingState()
}

data class MotorSettingUiState(
    val motor: Motor = Motor(),
)