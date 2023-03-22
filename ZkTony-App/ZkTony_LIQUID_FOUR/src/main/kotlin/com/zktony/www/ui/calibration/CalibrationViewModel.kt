package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseViewModel
import com.zktony.www.data.local.room.dao.CalibrationDao
import com.zktony.www.data.local.room.dao.CalibrationDataDao
import com.zktony.www.data.local.room.entity.Calibration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class CalibrationViewModel constructor(
    private val dao: CalibrationDao,
    private val dataDao: CalibrationDataDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<List<Calibration>?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().distinctUntilChanged().collect {
                _uiState.value = it
            }
        }
    }

    fun insert(name: String) {
        viewModelScope.launch {
            val cali = _uiState.value?.find { it.name == name }
            if (cali != null) {
                PopTip.show("校准程序名已存在")
            } else {
                val calibration = Calibration(name = name)
                dao.insert(calibration)
            }
        }
    }

    fun delete(calibration: Calibration) {
        viewModelScope.launch {
            dao.delete(calibration)
            dataDao.deleteBySubId(calibration.id)
            if (calibration.enable == 1) {
                val cali = _uiState.value?.find { it.name == "默认" }
                cali?.let { dao.update(it.copy(enable = 1)) }
            }
        }
    }

    fun enable(calibration: Calibration) {
        viewModelScope.launch {
            val cali = _uiState.value?.find { it.enable == 1 }
            if (cali == null) {
                dao.update(calibration.copy(enable = 1))
            } else {
                if (cali.id != calibration.id) {
                    dao.updateAll(
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

