package com.zktony.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.common.ext.Ext
import com.zktony.manager.common.ext.installApk
import com.zktony.manager.common.ext.showShortToast
import com.zktony.manager.common.http.DownloadManager
import com.zktony.manager.common.http.DownloadState
import com.zktony.manager.data.remote.grpc.ApplicationGrpc
import com.zktony.proto.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File

class SettingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState = _uiState.asStateFlow()



    fun navigateTo(page: SettingPage) {
        _uiState.value = _uiState.value.copy(page = page)
    }

}

data class SettingUiState(
    val page: SettingPage = SettingPage.SETTING,
)

enum class SettingPage {
    SETTING,
    USER_MODIFY,
    UPGRADE,
}