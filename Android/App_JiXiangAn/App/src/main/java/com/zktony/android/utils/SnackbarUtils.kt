package com.zktony.android.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SnackbarUtils {
    private val _state = MutableStateFlow<String?>(null)
    val state = _state.asStateFlow()

    fun showMessage(msg: String) {
        _state.value = msg
    }

    fun clearMessage() {
        _state.value = null
    }
}