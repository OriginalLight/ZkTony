package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.BuildConfig
import com.zktony.android.utils.SerialPortUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsVersionInfoViewModel @Inject constructor() : ViewModel() {
    private val _versionList = MutableStateFlow(listOf<String>())
    val versionList = _versionList.asStateFlow()

    init {
        viewModelScope.launch {
            _versionList.value = listOf(BuildConfig.VERSION_NAME)
            _versionList.value += SerialPortUtils.queryVersion(0, "B")
            repeat(4) {
                _versionList.value += SerialPortUtils.queryVersion(it)
            }
        }
    }
}