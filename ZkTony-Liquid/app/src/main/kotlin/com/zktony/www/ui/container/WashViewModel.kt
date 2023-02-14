package com.zktony.www.ui.container

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.room.entity.Plate
import com.zktony.www.control.motion.MotionManager
import com.zktony.www.control.serial.SerialManager
import com.zktony.www.common.repository.PlateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WashViewModel @Inject constructor(
    private val plateRepository: PlateRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<Plate?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            plateRepository.getPlateBySort(4).distinctUntilChanged().collect {
                _uiState.value = it
            }
        }
    }

    fun move(x: Float, y: Float) {
        val serial = SerialManager.instance
        if (serial.lock.value || serial.work.value) {
            PopTip.show("机器正在运行中")
            return
        }
        val m = MotionManager.instance
        m.executor(m.generator(x = x, y = y))
    }

    fun save(x: Float, y: Float) {
        viewModelScope.launch {
            _uiState.value?.let {
                plateRepository.updatePlate(it.copy(x1 = x, y1 = y), calculate = false)
            }
        }
    }
}