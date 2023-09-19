package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.SerialPortUtils.writeWithPulse
import com.zktony.android.utils.SerialPortUtils.writeWithValve
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author 刘贺贺
 * @date 2023/5/15 14:51
 */
@HiltViewModel
class DebugViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DebugUiState())
    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.DEBUG)
    private val _uiFlags = MutableStateFlow(UiFlags.NONE)
    private val _message = MutableStateFlow<String?>(null)

    val uiState = _uiState.asStateFlow()
    val message = _message.asStateFlow()

    init {
        viewModelScope.launch {
            combine(_selected, _page, _uiFlags) { selected, page, uiFlags ->
                DebugUiState(selected, page, uiFlags)
            }.catch { ex ->
                _message.value = ex.message
            }.collect {
                _uiState.value = it
            }
        }
    }


    fun uiEvent(uiEvent: DebugUiEvent) {
        when (uiEvent) {
            is DebugUiEvent.Message -> _message.value = uiEvent.message
            is DebugUiEvent.NavTo -> _page.value = uiEvent.page
            is DebugUiEvent.ToggleSelected -> _selected.value = uiEvent.id
            is DebugUiEvent.Valve -> viewModelScope.launch {
                try {
                    _uiFlags.value = UiFlags.VALVE
                    writeWithValve(uiEvent.id, uiEvent.value)
                } catch (ex: Exception) {
                    _message.value = ex.message
                } finally {
                    _uiFlags.value = UiFlags.NONE
                }
            }

            is DebugUiEvent.Pulse -> viewModelScope.launch {
                try {
                    _uiFlags.value = UiFlags.PUMP
                    writeWithPulse(uiEvent.id, uiEvent.value)
                } catch (ex: Exception) {
                    _message.value = ex.message
                } finally {
                    _uiFlags.value = UiFlags.NONE
                }
            }
        }
    }
}

data class DebugUiState(
    val selected: Long = 0L,
    val page: Int = PageType.DEBUG,
    val uiFlags: Int = UiFlags.NONE
)

sealed class DebugUiEvent {
    data class Message(val message: String?) : DebugUiEvent()
    data class NavTo(val page: Int) : DebugUiEvent()
    data class Pulse(val id: Int, val value: Long) : DebugUiEvent()
    data class ToggleSelected(val id: Long) : DebugUiEvent()
    data class Valve(val id: Int, val value: Int) : DebugUiEvent()
}