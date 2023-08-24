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
import com.zktony.android.utils.extra.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class SettingViewModel constructor(private val dao: MotorDao) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingUiState())
    private val _application = MutableStateFlow<Application?>(null)
    private val _selected = MutableStateFlow(0L)
    private val _progress = MutableStateFlow(0)
    private val _page = MutableStateFlow(PageType.SETTINGS)

    val uiState = _uiState.asStateFlow()

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
                    SettingUiState(
                        application = application,
                        entities = entities,
                        selected = selected,
                        progress = progress,
                        page = page
                    )
                }.catch { ex ->
                    ex.printStackTrace()
                }.collect {
                    _uiState.value = it
                }
            }
            launch {
                httpCall {
                    _application.value =
                        it.find { app -> app.application_id == BuildConfig.APPLICATION_ID }
                }
            }
        }
    }

    fun uiEvent(event: SettingUiEvent) {
        when (event) {
            is SettingUiEvent.NavTo -> _page.value = event.page
            is SettingUiEvent.Navigation -> navigation(event.navigation)
            is SettingUiEvent.Network -> network()
            is SettingUiEvent.CheckUpdate -> checkUpdate()
            is SettingUiEvent.ToggleSelected -> _selected.value = event.id
            is SettingUiEvent.Update -> viewModelScope.launch { dao.update(event.entity) }
        }
    }

    private fun navigation(nav: Boolean) {
        viewModelScope.launch {
            val intent = Intent().apply {
                action = "ACTION_SHOW_NAVBAR"
                putExtra("cmd", if (nav) "show" else "hide")
            }
            Ext.ctx.sendBroadcast(intent)
        }
    }

    private fun network() {
        // Create a new intent to launch the Wi-Fi settings screen
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            // Set the extra preferences to show the button bar and custom text
            putExtra("extra_prefs_show_button_bar", true)
            putExtra("extra_prefs_set_next_text", Ext.ctx.getString(R.string.finish))
            putExtra("extra_prefs_set_back_text", Ext.ctx.getString(R.string.cancel))
        }
        // Launch the Wi-Fi settings screen
        Ext.ctx.startActivity(intent)
    }

    private fun checkUpdate() {
        viewModelScope.launch {
            val application = _application.value
            if (application != null) {
                if (application.version_code > BuildConfig.VERSION_CODE
                    && application.download_url.isNotEmpty()
                    && _progress.value == 0
                ) {
                    download(application.download_url)
                    _progress.value = 1
                }
            } else {
                httpCall {
                    it.find { app -> app.application_id == BuildConfig.APPLICATION_ID }
                }
            }
        }
    }

    private fun download(url: String) {
        viewModelScope.launch {
            url.download(File(Ext.ctx.getExternalFilesDir(null), "update.apk"))
                .collect {
                    when (it) {
                        is DownloadState.Success -> {
                            _progress.value = 0
                            Ext.ctx.installApk(it.file)
                        }

                        is DownloadState.Err -> {
                            _progress.value = 0
                            Ext.ctx.getString(R.string.downloading).showShortToast()
                        }

                        is DownloadState.Progress -> {
                            _progress.value = maxOf(it.progress, 1)
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
    val page: PageType = PageType.SETTINGS,
)


sealed class SettingUiEvent {
    data object Network : SettingUiEvent()
    data object CheckUpdate : SettingUiEvent()
    data class NavTo(val page: PageType) : SettingUiEvent()
    data class Navigation(val navigation: Boolean) : SettingUiEvent()
    data class ToggleSelected(val id: Long) : SettingUiEvent()
    data class Update(val entity: Motor) : SettingUiEvent()
}