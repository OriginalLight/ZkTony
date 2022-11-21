package com.zktony.www.ui.admin

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.room.entity.Motor
import com.zktony.www.data.repository.MotorRepository
import com.zktony.www.serialport.SerialPortManager
import com.zktony.www.serialport.getSerialPort
import com.zktony.www.serialport.protocol.Command
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotorViewModel @Inject constructor(
    private val repo: MotorRepository
) : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

    private val _motorList = MutableStateFlow(emptyList<Motor>())
    private val _editMotor = MutableStateFlow(Motor())
    private val _selectedMotor = MutableStateFlow(Motor())
    val motorList = _motorList.asStateFlow()
    val editMotor = _editMotor.asStateFlow()
    val selectedMotor = _selectedMotor.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAll().collect { motors ->
                _motorList.value = motors
                _editMotor.value =
                    if (editMotor.value.name.isEmpty()) motors.first() else editMotor.value
                _selectedMotor.value =
                    if (selectedMotor.value.name.isEmpty()) motors.first() else selectedMotor.value
            }
        }
    }

    /**
     * 电机修改参数
     * @param motor [Motor]
     */
    fun motorValueChange(motor: Motor) {
        _editMotor.value = motor
    }

    /**
     * 编辑电机
     * @param motor [Motor]
     */
    fun selectMotor(motor: Motor) {
        _editMotor.value = motor
        _selectedMotor.value = motor
    }

    /**
     * 更新电机
     */
    fun updateMotor() {
        viewModelScope.launch {
            if (validateMotor(editMotor.value)) {
                repo.update(editMotor.value)
                SerialPortManager.instance.sendHex(
                    getSerialPort(editMotor.value.board),
                    Command(
                        parameter = "04",
                        data = editMotor.value.toHex()
                    ).toHex()
                )
                PopTip.show("更新成功")
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
                PopTip.show("速度不能小于0")
            }
            return false
        }
        if (motor.acceleration > 100 || motor.acceleration < 10) {
            viewModelScope.launch {
                PopTip.show("加速度范围为10-100")
            }
            return false
        }
        if (motor.deceleration > 100 || motor.deceleration < 10) {
            viewModelScope.launch {
                PopTip.show("减速度范围为10-100")
            }
            return false
        }

        return true
    }
}