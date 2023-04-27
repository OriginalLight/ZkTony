package com.zktony.www.ui.admin

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.R
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.datastore.ext.read
import com.zktony.datastore.ext.save
import com.zktony.www.common.ext.decideLock
import com.zktony.www.common.ext.execute
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
                DS.read("MAX_Y_TRIP", 200f).collect {
                    _uiState.value = _uiState.value.copy(maxYTrip = it)
                }
            }
            launch {
                DS.read("WASH_TANK", 0f).collect {
                    _uiState.value = _uiState.value.copy(washTank = it)
                }
            }
        }
    }

    fun move(yAxis: Float) {
        viewModelScope.launch {
            decideLock {
                yes {
                    PopTip.show(Ext.ctx.getString(R.string.running))
                }
                no {
                    execute {
                        step {
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
    val maxYTrip: Float = 200f,
    val washTank: Float = 0f,
)