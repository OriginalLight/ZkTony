package com.zktony.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.data.remote.grpc.ApplicationGrpc
import com.zktony.manager.ext.*
import com.zktony.proto.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File

class UpgradeViewModel constructor(
    private val grpc: ApplicationGrpc,
) : ViewModel() {


    private val _uiState = MutableStateFlow(UpgradeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                grpc.getByApplicationId()
                    .flowOn(Dispatchers.IO)
                    .catch { it.message.toString().showShortToast() }
                    .collect {
                        _uiState.value = _uiState.value.copy(application = it)
                    }
            }
        }
    }


    fun upgrade() {
        viewModelScope.launch {
            if (_uiState.value.download) {
                return@launch
            }
            _uiState.value = _uiState.value.copy(download = true)
            _uiState.value.application?.let { app ->
                app.downloadUrl.download(File(Ext.ctx.getExternalFilesDir(null), "app.apk"))
                    .flowOn(Dispatchers.IO)
                    .collect {
                        when (it) {
                            is DownloadState.Success -> {
                                _uiState.value = _uiState.value.copy(download = false)
                                // install apk
                                Ext.ctx.installApk(it.file)
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

data class UpgradeUiState(
    val download: Boolean = false,
    val progress: Int = 0,
    val application: Application? = null,
)