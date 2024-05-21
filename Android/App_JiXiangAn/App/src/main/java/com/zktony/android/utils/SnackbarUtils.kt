package com.zktony.android.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SnackbarUtils {
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun showMessage(msg: String) {
        _message.value = msg
    }

    fun clearMessage() {
        _message.value = null
    }
}