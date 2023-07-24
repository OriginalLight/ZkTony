package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.ext.dsl.tx
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            _loading.collect {
                _uiState.value = ConfigUiState(loading = it)
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
            is ConfigEvent.MoveTo -> moveTo(event.index, event.distance)
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
 * @param loading The loading state of the screen.
 */
data class ConfigUiState(
    val loading: Boolean = false,
)

/**
 * Represents an event that can occur on the Config screen.
 */
sealed class ConfigEvent {
    data class MoveTo(val index: Int, val distance: Float) : ConfigEvent()
}