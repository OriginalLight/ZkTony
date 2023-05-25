package com.zktony.www.ui.calibration

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.www.data.dao.CalibrationDao
import com.zktony.www.data.entities.Calibration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CalibrationViewModel constructor(
    private val CD: CalibrationDao,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<List<Calibration>>(emptyList())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            CD.getAll().collect {
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
            val cali = _uiState.value.find { it.name == name }
            if (cali != null) {
                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.already_exists))
            } else {
                val model = Calibration(name = name)
                CD.insert(model)
                block(model.id)
            }
        }
    }

    fun delete(calibration: Calibration) {
        viewModelScope.launch {
            CD.delete(calibration)
        }
    }

    fun enable(calibration: Calibration) {
        viewModelScope.launch {
            val cali = _uiState.value.find { it.active == 1 }
            if (cali == null) {
                CD.update(calibration.copy(active = 1))
            } else {
                if (cali.id != calibration.id) {
                    CD.updateAll(
                        listOf(
                            cali.copy(active = 0),
                            calibration.copy(active = 1)
                        )
                    )
                }
            }
        }
    }
}

