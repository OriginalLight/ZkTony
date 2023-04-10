package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.www.room.dao.CalibrationDao
import com.zktony.www.room.dao.CalibrationDataDao
import com.zktony.www.room.entity.Calibration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class CalibrationViewModel constructor(
    private val calibrationDao: CalibrationDao,
    private val calibrationDataDao: CalibrationDataDao,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<List<Calibration>?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            calibrationDao.getAll().distinctUntilChanged().collect {
                _uiState.value = it
            }
        }
    }

    fun insert(name: String, block: (Long) -> Unit) {
        viewModelScope.launch {
            val cali = _uiState.value?.find { it.name == name }
            if (cali != null) {
                PopTip.show("校准程序名已存在")
            } else {
                val calibration = Calibration(name = name)
                calibrationDao.insert(calibration)
                block(calibration.id)
            }
        }
    }

    fun delete(calibration: Calibration) {
        viewModelScope.launch {
            calibrationDao.delete(calibration)
            calibrationDataDao.deleteBySubId(calibration.id)
            if (calibration.enable == 1) {
                val cali = _uiState.value?.find { it.name == "默认" }
                cali?.let { calibrationDao.update(it.copy(enable = 1)) }
            }
        }
    }

    fun enable(calibration: Calibration) {
        viewModelScope.launch {
            val cali = _uiState.value?.find { it.enable == 1 }
            if (cali == null) {
                calibrationDao.update(calibration.copy(enable = 1))
            } else {
                if (cali.id != calibration.id) {
                    calibrationDao.updateAll(
                        listOf(
                            cali.copy(enable = 0),
                            calibration.copy(enable = 1)
                        )
                    )
                }
            }
        }
    }

}
