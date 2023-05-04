package com.zktony.www.ui.admin

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.www.room.dao.MotorDao
import com.zktony.www.room.entity.Motor
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MotorViewModel constructor(
    private val MD: MotorDao,
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
                _uiState.value = _uiState.value.copy(motor = it.copy(acc = i))
            }
        }
    }

    fun deceleration(i: Int) {
        viewModelScope.launch {
            _uiState.value.motor?.let {
                _uiState.value = _uiState.value.copy(motor = it.copy(dec = i))
            }
        }
    }


    /**
     * 更新电机
     */
    fun update() {
        viewModelScope.launch {
            _uiState.value.motor?.let {
                MD.update(it)
                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.update_success))
            }
        }
    }
}

data class MotorUiState(
    val motorList: List<Motor> = emptyList(),
    val motor: Motor? = null,
)