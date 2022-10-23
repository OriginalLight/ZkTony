package com.zktony.www.ui.admin

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppIntent
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.data.entity.Motor
import com.zktony.www.data.repository.MotorRepository
import com.zktony.www.model.enum.getSerialPortEnum
import com.zktony.www.serialport.protocol.Command
import com.zktony.www.ui.admin.model.MotorSettingIntent
import com.zktony.www.ui.admin.model.MotorSettingState
import com.zktony.www.ui.admin.model.MotorSettingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    private val intent = MutableSharedFlow<MotorSettingIntent>()

    private val _uiState = MutableStateFlow(MotorSettingUiState())
    val uiState: StateFlow<MotorSettingUiState> get() = _uiState

    init {
        viewModelScope.launch {
            launch {
                intent.collect {
                    when (it) {
                        is MotorSettingIntent.OnMotorValueChange -> onMotorValueChange(it.motor)
                        is MotorSettingIntent.OnEditMotor -> onEditMotor(it.motor)
                        is MotorSettingIntent.OnUpdateMotor -> onUpdateMotor()
                    }
                }
            }
            launch {
                delay(200L)
                motorRepository.getAll().collect { motors ->
                    _uiState.update {
                        if (it.motor.name.isEmpty()) {
                            it.copy(motor = motors.first())
                        } else {
                            it
                        }
                    }
                    _state.emit(MotorSettingState.OnMotorValueChange(uiState.value.motor))
                    _state.emit(MotorSettingState.OnDataBaseChange(motors))
                }
            }

        }
    }

    /**
     * Intent处理器
     * @param intent [MotorSettingIntent]
     */
    fun dispatch(intent: MotorSettingIntent) {
        try {
            viewModelScope.launch {
                this@MotorSettingViewModel.intent.emit(intent)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * 电机修改参数
     * @param motor [Motor]
     */
    private fun onMotorValueChange(motor: Motor) {
        _uiState.update {
            it.copy(motor = motor)
        }
    }

    /**
     * 编辑电机
     * @param motor [Motor]
     */
    private fun onEditMotor(motor: Motor) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(motor = motor)
            }
            _state.emit(MotorSettingState.OnMotorValueChange(motor))
        }
    }

    /**
     * 更新电机
     */
    private fun onUpdateMotor() {
        viewModelScope.launch {
            val motor = uiState.value.motor
            if (validateMotor(motor)) {
                motorRepository.update(motor)
                appViewModel.dispatch(
                    AppIntent.Sender(
                        getSerialPortEnum(motor.board),
                        Command(
                            parameter = "04",
                            data = motor.toHex()
                        ).toHex()
                    )
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