package com.zktony.android.ui

import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.entities.Motor
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.extra.Application
import com.zktony.android.utils.extra.DownloadState
import com.zktony.android.utils.extra.download
import com.zktony.android.utils.extra.httpCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
@HiltViewModel
class SettingViewModel @Inject constructor(
    private val dao: MotorDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingUiState())
    private val _application = MutableStateFlow<Application?>(null)
    private val _selected = MutableStateFlow(0L)
    private val _progress = MutableStateFlow(0)
    private val _page = MutableStateFlow(PageType.SETTINGS)
    private val _message = MutableStateFlow<String?>(null)

    val uiState = _uiState.asStateFlow()
    val message = _message.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                combine(
                    _application,
                    dao.getAll(),
                    _selected,
                    _progress,
                    _page
                ) { application, entities, selected, progress, page ->
                    SettingUiState(application, entities, selected, progress, page)
                }.catch { ex ->
                    _message.value = ex.message
                }.collect {
                    _uiState.value = it
                }
            }
            launch {
                if (ApplicationUtils.isNetworkAvailable()) {
                    httpCall { _application.value = it }
                }
            }
        }
    }

    fun uiEvent(uiEvent: SettingUiEvent) {
        when (uiEvent) {
            is SettingUiEvent.CheckUpdate -> checkUpdate()
            is SettingUiEvent.Delete -> viewModelScope.launch { dao.deleteById(uiEvent.id) }
            is SettingUiEvent.Insert -> viewModelScope.launch { dao.insert(Motor(displayText = "None")) }
            is SettingUiEvent.Message -> _message.value = uiEvent.message
            is SettingUiEvent.Navigation -> navigation(uiEvent.navigation)
            is SettingUiEvent.NavTo -> _page.value = uiEvent.page
            is SettingUiEvent.Network -> network()
            is SettingUiEvent.ToggleSelected -> _selected.value = uiEvent.id
            is SettingUiEvent.Update -> viewModelScope.launch { dao.update(uiEvent.entity) }
        }
    }

    private fun navigation(nav: Boolean) {
        viewModelScope.launch {
            val intent = Intent().apply {
                action = "ACTION_SHOW_NAVBAR"
                putExtra("cmd", if (nav) "show" else "hide")
            }
            ApplicationUtils.ctx.sendBroadcast(intent)
        }
    }

    private fun network() {
        // Create a new intent to launch the Wi-Fi settings screen
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            // Set the extra preferences to show the button bar and custom text
            putExtra("extra_prefs_show_button_bar", true)
            putExtra("extra_prefs_set_next_text", ApplicationUtils.ctx.getString(R.string.finish))
            putExtra("extra_prefs_set_back_text", ApplicationUtils.ctx.getString(R.string.cancel))
        }
        // Launch the Wi-Fi settings screen
        ApplicationUtils.ctx.startActivity(intent)
    }

    private fun checkUpdate() {
        viewModelScope.launch {
            val application = _application.value
            if (application != null) {
                if (application.versionCode > BuildConfig.VERSION_CODE
                    && application.downloadUrl.isNotEmpty()
                    && _progress.value == 0
                ) {
                    _progress.value = 1
                    application.downloadUrl.download(
                        File(
                            ApplicationUtils.ctx.getExternalFilesDir(null),
                            "update.apk"
                        )
                    ).collect {
                        when (it) {
                            is DownloadState.Success -> {
                                _progress.value = 0
                                ApplicationUtils.installApp(it.file)
                            }

                            is DownloadState.Err -> {
                                _progress.value = 0
                                _message.value = "下载失败: ${it.t.message}"
                            }

                            is DownloadState.Progress -> {
                                _progress.value = maxOf(it.progress, 1)
                            }
                        }
                    }
                }
            } else {
                httpCall(exception = { _message.value = it.message }) { app ->
                    if (app != null) {
                        _application.value = app
                    } else {
                        _message.value = "未找到升级信息"
                    }
                }
            }
        }
    }
}

data class SettingUiState(
    val application: Application? = null,
    val entities: List<Motor> = emptyList(),
    val selected: Long = 0,
    val progress: Int = 0,
    val page: Int = PageType.SETTINGS
)

sealed class SettingUiEvent {
    data class Delete(val id: Long) : SettingUiEvent()
    data class Message(val message: String?) : SettingUiEvent()
    data class Navigation(val navigation: Boolean) : SettingUiEvent()
    data class NavTo(val page: Int) : SettingUiEvent()
    data class ToggleSelected(val id: Long) : SettingUiEvent()
    data class Update(val entity: Motor) : SettingUiEvent()
    data object CheckUpdate : SettingUiEvent()
    data object Insert : SettingUiEvent()
    data object Network : SettingUiEvent()
}