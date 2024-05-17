package com.zktony.android.ui

import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.entities.Motor
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.extra.Application
import com.zktony.android.utils.extra.DownloadState
import com.zktony.android.utils.extra.download
import com.zktony.android.utils.extra.httpCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _application = MutableStateFlow<Application?>(null)
    private val _selected = MutableStateFlow(0L)
    private val _progress = MutableStateFlow(0)
    private val _page = MutableStateFlow(PageType.SETTINGS)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())

    val application = _application.asStateFlow()
    val selected = _selected.asStateFlow()
    val progress = _progress.asStateFlow()
    val page = _page.asStateFlow()
    val uiFlags = _uiFlags.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            if (ApplicationUtils.isNetworkAvailable()) {
                httpCall { _application.value = it }
            }
        }
    }

    fun dispatch(intent: SettingIntent) {
        when (intent) {
            is SettingIntent.CheckUpdate -> checkUpdate()
            is SettingIntent.Delete -> viewModelScope.launch { dao.deleteById(intent.id) }
            is SettingIntent.Insert -> viewModelScope.launch { dao.insert(Motor(displayText = "None")) }
            is SettingIntent.Flags -> _uiFlags.value = intent.uiFlags
            is SettingIntent.Navigation -> navigation(intent.navigation)
            is SettingIntent.NavTo -> _page.value = intent.page
            is SettingIntent.Network -> network()
            is SettingIntent.Selected -> _selected.value = intent.id
            is SettingIntent.Update -> viewModelScope.launch { dao.update(intent.entity) }
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
                                _uiFlags.value = UiFlags.message("下载失败: ${it.t.message}")
                            }

                            is DownloadState.Progress -> {
                                _progress.value = maxOf(it.progress, 1)
                            }
                        }
                    }
                }
            } else {
                httpCall(exception = { _uiFlags.value = UiFlags.message(it.message ?: "Unknown") }) { app ->
                    if (app != null) {
                        _application.value = app
                    } else {
                        _uiFlags.value = UiFlags.message("未找到升级信息")
                    }
                }
            }
        }
    }
}

sealed class SettingIntent {
    data class Delete(val id: Long) : SettingIntent()
    data class Flags(val uiFlags: UiFlags) : SettingIntent()
    data class Navigation(val navigation: Boolean) : SettingIntent()
    data class NavTo(val page: Int) : SettingIntent()
    data class Selected(val id: Long) : SettingIntent()
    data class Update(val entity: Motor) : SettingIntent()
    data object CheckUpdate : SettingIntent()
    data object Insert : SettingIntent()
    data object Network : SettingIntent()
}