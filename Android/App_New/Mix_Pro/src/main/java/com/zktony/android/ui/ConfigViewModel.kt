package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.core.dsl.tx
import com.zktony.datastore.ext.saveSettings
import com.zktony.datastore.ext.settingsFlow
import com.zktony.proto.SettingsPreferences
import com.zktony.proto.copy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class ConfigViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ConfigUiState())
    private val _lock = MutableStateFlow(false)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsFlow,
                _lock
            ) { settings, lock ->
                ConfigUiState(settings = settings, lock = lock)
            }.catch { ex ->
                ex.printStackTrace()
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun event(event: ConfigEvent) {
        when (event) {
            is ConfigEvent.SetTravel -> setTravel(event.index, event.distance)
            is ConfigEvent.SetWaste -> setWaste(event.index, event.distance)
            is ConfigEvent.MoveTo -> moveTo(event.index, event.distance)
        }
    }

    private fun setTravel(index: Int, distance: Float) {
        viewModelScope.launch {
            val list = _uiState.value.settings.travelList.toMutableList()
            if (list.size == 0) {
                repeat(3) {
                    list.add(0f)
                }
            }
            list[index] = distance
            saveSettings {
                it.copy {
                    travel.clear()
                    travel.addAll(list)
                }
            }
        }
    }

    private fun setWaste(index: Int, distance: Float) {
        viewModelScope.launch {
            val list = _uiState.value.settings.wasteList.toMutableList()
            if (list.size == 0) {
                repeat(3) {
                    list.add(0f)
                }
            }
            list[index] = distance
            saveSettings {
                it.copy {
                    waste.clear()
                    waste.addAll(list)
                }
            }
        }
    }

    private fun moveTo(index: Int, distance: Float) {
        viewModelScope.launch {
            _lock.value = true
            tx {
                mdm {
                    this.index = index + 3
                    dv = distance
                }
            }
            _lock.value = false
        }
    }

}

data class ConfigUiState(
    val settings: SettingsPreferences = SettingsPreferences.getDefaultInstance(),
    val lock: Boolean = false,
)

sealed class ConfigEvent {
    data class SetTravel(val index: Int, val distance: Float) : ConfigEvent()
    data class SetWaste(val index: Int, val distance: Float) : ConfigEvent()
    data class MoveTo(val index: Int, val distance: Float) : ConfigEvent()
}