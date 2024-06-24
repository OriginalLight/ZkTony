package com.zktony.android.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SnackbarUtils {
    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar = _snackbar.asStateFlow()

    fun showSnackbar(msg: String) {
        _snackbar.value = msg
    }

    fun clearSnackbar() {
        _snackbar.value = null
    }
}