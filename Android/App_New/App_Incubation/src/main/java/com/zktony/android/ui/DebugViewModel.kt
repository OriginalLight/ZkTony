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
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author 刘贺贺
 * @date 2023/5/15 14:51
 */
@HiltViewModel
class DebugViewModel @Inject constructor() : ViewModel() {

    private val _page = MutableStateFlow(PageType.DEBUG)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())

    val page = _page.asStateFlow()
    val uiFlags = _uiFlags.asStateFlow()

    fun dispatch(intent: DebugIntent) {
        when (intent) {
            is DebugIntent.Flags -> _uiFlags.value = intent.uiFlags
            is DebugIntent.NavTo -> _page.value = intent.page
            is DebugIntent.Valve -> valve(intent.index, intent.value)
            is DebugIntent.Transfer -> transfer(intent.index, intent.turns)
        }
    }

    private fun valve(index: Int, value: Int) {
        viewModelScope.launch {
            try {
                _uiFlags.value = UiFlags.loading()
                writeWithValve(index, value)
                _uiFlags.value = UiFlags.none()
            } catch (ex: Exception) {
                _uiFlags.value = UiFlags.message(ex.message ?: "Unknown")
            }
        }
    }

    private fun transfer(index: Int, turns: Double) {
        viewModelScope.launch {
            try {
                _uiFlags.value = UiFlags.loading()
                writeWithPulse(index, (turns * 6400).toLong())
                _uiFlags.value = UiFlags.none()
            } catch (ex: Exception) {
                _uiFlags.value = UiFlags.message(ex.message ?: "Unknown")
            }
        }
    }
}

sealed class DebugIntent {
    data class Flags(val uiFlags: UiFlags) : DebugIntent()
    data class NavTo(val page: Int) : DebugIntent()
    data class Transfer(val index: Int, val turns: Double) : DebugIntent()
    data class Valve(val index: Int, val value: Int) : DebugIntent()
}