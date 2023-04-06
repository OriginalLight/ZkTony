package com.zktony.manager.ui.screen.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.common.ext.installApk
import com.zktony.manager.common.http.DownloadManager
import com.zktony.manager.common.http.DownloadState
import com.zktony.manager.data.local.dao.UserDao
import com.zktony.manager.data.local.model.User
import com.zktony.manager.data.remote.ApplicationGrpc
import com.zktony.proto.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File

class SettingViewModel constructor(
    private val dao: UserDao,
    private val grpc: ApplicationGrpc,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                dao.getAll().collect {
                    if (it.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(user = it.first())
                    }
                }
            }
            launch {
                grpc.getByApplicationId().collect {
                    _uiState.value = _uiState.value.copy(application = it)
                }
            }
        }
    }

    fun navigateTo(page: SettingPage) {
        _uiState.value = _uiState.value.copy(page = page)
    }

    fun saveUser() {
        viewModelScope.launch {
            dao.insert(_uiState.value.user)
        }
    }

    fun onUserChange(user: User) {
        _uiState.value = _uiState.value.copy(user = user)
    }

    fun update(context: Context) {
        viewModelScope.launch {
            if (_uiState.value.download) {
                return@launch
            }
            _uiState.value = _uiState.value.copy(download = true)
            _uiState.value.application?.let { app ->
                DownloadManager.download(
                    url = app.downloadUrl,
                    file = File(context.getExternalFilesDir(null), "app.apk"),
                ).flowOn(Dispatchers.IO)
                    .collect {
                        when (it) {
                            is DownloadState.Success -> {
                                _uiState.value = _uiState.value.copy(download = false)
                                // install apk
                                context.installApk(it.file)
                            }
                            is DownloadState.Err -> {
                                _uiState.value = _uiState.value.copy(download = false)
                            }

                            is DownloadState.Progress -> {
                                _uiState.value = _uiState.value.copy(
                                    progress = it.progress
                                )
                            }
                        }
                    }

            }
        }
    }

}

data class SettingUiState(
    val download: Boolean = false,
    val loading: Boolean = false,
    val progress: Int = 0,
    val error: String? = null,
    val page: SettingPage = SettingPage.SETTING,
    val user: User = User(),
    val application: Application? = null,
)

enum class SettingPage {
    SETTING, USER_MODIFY
}