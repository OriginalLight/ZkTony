package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.SerialPortUtils.writeWithPulse
import com.zktony.android.utils.SerialPortUtils.writeWithValve
import com.zktony.android.utils.SnackbarUtils
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
    private val _loading = MutableStateFlow(false)

    val page = _page.asStateFlow()
    val loading = _loading.asStateFlow()

    fun dispatch(intent: DebugIntent) {
        when (intent) {
            is DebugIntent.Valve -> valve(intent.value)
            is DebugIntent.Transfer -> transfer(intent.turns)
        }
    }

    private fun valve(value: Int) {
        viewModelScope.launch {
            try {
                _loading.value = true
                writeWithValve(1, value)
                _loading.value = false
            } catch (ex: Exception) {
                SnackbarUtils.showMessage(ex.message ?: "Unknown")
                _loading.value = false
            }
        }
    }

    private fun transfer(turns: Double) {
        viewModelScope.launch {
            try {
                _loading.value = true
                writeWithPulse(1, (turns * 6400).toLong())
                _loading.value = false
            } catch (ex: Exception) {
                SnackbarUtils.showMessage(ex.message ?: "Unknown")
                _loading.value = false
            }
        }
    }
}

sealed class DebugIntent {
    data class Valve(val value: Int) : DebugIntent()
    data class Transfer(val turns: Double) : DebugIntent()
}