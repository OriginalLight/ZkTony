package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.utils.tx.tx
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Config screen.
 */
class ConfigViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigUiState())
    private val _loading = MutableStateFlow(false)

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _loading.collect {
                _uiState.value = ConfigUiState(loading = it)
            }
        }
    }

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
                move {
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