package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.www.data.dao.CalibrationDao
import com.zktony.www.data.dao.CalibrationDataDao
import com.zktony.www.data.entities.Calibration
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CalibrationViewModel constructor(
    private val CD: CalibrationDao,
    private val CDD: CalibrationDataDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<List<Calibration>?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            CD.getAll().distinctUntilChanged().collect {
                _uiState.value = it
            }
        }
    }

    fun insert(name: String, block: (Long) -> Unit) {
        viewModelScope.launch {
            if (name.isEmpty()) {
                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.not_empty))
                return@launch
            }
            val cali = _uiState.value?.find { it.name == name }
            if (cali != null) {
                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.already_exists))
            } else {
                val calibration = Calibration(name = name)
                CD.insert(calibration)
                block(calibration.id)
            }
        }
    }

    fun delete(calibration: Calibration) {
        viewModelScope.launch {
            CD.delete(calibration)
            CDD.deleteBySubId(calibration.id)
            if (calibration.enable == 1) {
                val cali =
                    _uiState.value?.find { it.name == Ext.ctx.getString(com.zktony.core.R.string.def) }
                cali?.let { CD.update(it.copy(enable = 1)) }
            }
        }
    }

    fun enable(calibration: Calibration) {
        viewModelScope.launch {
            val cali = _uiState.value?.find { it.enable == 1 }
            if (cali == null) {
                CD.update(calibration.copy(enable = 1))
            } else {
                if (cali.id != calibration.id) {
                    CD.updateAll(
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

