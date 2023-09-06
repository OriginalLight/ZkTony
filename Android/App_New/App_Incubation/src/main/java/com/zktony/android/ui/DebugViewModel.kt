package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.extra.valve
import com.zktony.android.utils.extra.writeRegister
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
    private val _uiFlags = MutableStateFlow(0)
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
            is DebugUiEvent.NavTo -> _page.value = uiEvent.page
            is DebugUiEvent.ToggleSelected -> _selected.value = uiEvent.id
            is DebugUiEvent.Valve -> viewModelScope.launch {
                _uiFlags.value = 1
                valve(uiEvent.id, uiEvent.value)
                _uiFlags.value = 0
            }

            is DebugUiEvent.Pulse -> viewModelScope.launch {
                _uiFlags.value = 1
                writeRegister(startAddr = 222, slaveAddr = uiEvent.id, value = uiEvent.value)
                _uiFlags.value = 0
            }
        }
    }
}

data class DebugUiState(
    val selected: Long = 0L,
    val page: PageType = PageType.DEBUG,
    val uiFlags: Int = 0
)

sealed class DebugUiEvent {
    data class NavTo(val page: PageType) : DebugUiEvent()
    data class ToggleSelected(val id: Long) : DebugUiEvent()
    data class Valve(val id: Int, val value: Int) : DebugUiEvent()
    data class Pulse(val id: Int, val value: Long) : DebugUiEvent()
}