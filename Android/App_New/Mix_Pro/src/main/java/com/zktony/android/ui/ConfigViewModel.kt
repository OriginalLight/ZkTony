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
 * ViewModel for the Config screen.
 */
class ConfigViewModel : ViewModel() {

    /**
     * Represents the current UI state of the Config screen.
     */
    private val _uiState = MutableStateFlow(ConfigUiState())

    /**
     * Represents the current loading state of the Config screen.
     */
    private val _loading = MutableStateFlow(false)

    /**
     * Exposes the current UI state of the Config screen as a read-only flow.
     */
    val uiState = _uiState.asStateFlow()

    /**
     * Initializes the view model by observing changes to the settings and updating the UI state accordingly.
     */
    init {
        viewModelScope.launch {
            combine(
                settingsFlow,
                _loading
            ) { settings, loading ->
                ConfigUiState(settings = settings, loading = loading)
            }.catch { ex ->
                ex.printStackTrace()
            }.collect {
                _uiState.value = it
            }
        }
    }

    /**
     * Handles the given event and updates the UI state accordingly.
     *
     * @param event The event to handle.
     */
    fun event(event: ConfigEvent) {
        when (event) {
            is ConfigEvent.SetTravel -> setTravel(event.index, event.distance)
            is ConfigEvent.SetWaste -> setWaste(event.index, event.distance)
            is ConfigEvent.MoveTo -> moveTo(event.index, event.distance)
        }
    }

    /**
     * Sets the travel distance for the given index in the settings.
     *
     * @param index The index of the travel distance to set.
     * @param distance The distance to set.
     */
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

    /**
     * Sets the waste distance for the given index in the settings.
     *
     * @param index The index of the waste distance to set.
     * @param distance The distance to set.
     */
    private fun setWaste(index: Int, distance: Float) {
        viewModelScope.launch {
            val list = _uiState.value.settings.wasteList.toMutableList()
            if (list.size == 0) {
                repeat(2) {
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

    /**
     * Moves to the given index with the given distance.
     *
     * @param index The index to move to.
     * @param distance The distance to move.
     */
    private fun moveTo(index: Int, distance: Float) {
        viewModelScope.launch {
            _loading.value = true
            tx {
                mdm {
                    this.index = index + 3
                    dv = distance
                }
            }
            _loading.value = false
        }
    }

}

/**
 * Represents the current UI state of the Config screen.
 *
 * @param settings The settings to display.
 * @param loading The loading state of the screen.
 */
data class ConfigUiState(
    val settings: SettingsPreferences = SettingsPreferences.getDefaultInstance(),
    val loading: Boolean = false,
)

/**
 * Represents an event that can occur on the Config screen.
 */
sealed class ConfigEvent {
    data class SetTravel(val index: Int, val distance: Float) : ConfigEvent()
    data class SetWaste(val index: Int, val distance: Float) : ConfigEvent()
    data class MoveTo(val index: Int, val distance: Float) : ConfigEvent()
}