package com.zktony.www.ui.admin

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.datastore.ext.read
import com.zktony.datastore.ext.save
import com.zktony.www.core.ext.decideLock
import com.zktony.www.core.ext.execute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConfigViewModel(
    private val DS: DataStore<Preferences>,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ConfigUiStatus())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                DS.read("MAX_X_TRIP", 160f).collect {
                    _uiState.value = _uiState.value.copy(maxXTrip = it)
                }
            }
            launch {
                DS.read("MAX_Y_TRIP", 200f).collect {
                    _uiState.value = _uiState.value.copy(maxYTrip = it)
                }
            }
            launch {
                DS.read("WASH_X_AXIS", 0f).collect {
                    _uiState.value = _uiState.value.copy(washXAxis = it)
                }
            }
            launch {
                DS.read("WASH_Y_AXIS", 0f).collect {
                    _uiState.value = _uiState.value.copy(washYAxis = it)
                }
            }
        }
    }

    fun move(xAxis: Float, yAxis: Float) {
        viewModelScope.launch {
            decideLock {
                yes { PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.running)) }
                no {
                    execute {
                        step {
                            x = xAxis
                            y = yAxis
                        }
                    }
                }
            }
        }
    }

    fun save(key: String, value: Float) {
        viewModelScope.launch {
            DS.save(key, value)
        }
    }
}

data class ConfigUiStatus(
    val maxXTrip: Float = 160f,
    val maxYTrip: Float = 200f,
    val washXAxis: Float = 0f,
    val washYAxis: Float = 0f,
)