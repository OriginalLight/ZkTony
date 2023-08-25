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
            // Combine the application, settings, progress, and page values into a single UI state
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
            // Load the latest application instance from the server if the network is available
            launch {
                httpCall {
                    _application.value =
                        it.find { app -> app.application_id == BuildConfig.APPLICATION_ID }
                }
            }
        }
    }

    /**
     * Handles the specified setting event and updates the UI state accordingly.
     *
     * @param event The setting event to handle.
     */
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

    /**
     * Toggles the navigation bar on or off.
     *
     * @param nav Whether to show or hide the navigation bar.
     */
    private fun navigation(nav: Boolean) {
        viewModelScope.launch {
            // Create an intent to show or hide the navigation bar
            val intent = Intent().apply {
                action = "ACTION_SHOW_NAVBAR"
                putExtra("cmd", if (nav) "show" else "hide")
            }

            // Send the broadcast to show or hide the navigation bar
            Ext.ctx.sendBroadcast(intent)
        }
    }

    /**
     * Launches the Wi-Fi settings screen to allow the user to configure their network settings.
     */
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

    /**
     * Checks for updates and downloads the latest version of the application if available.
     */
    private fun checkUpdate() {
        viewModelScope.launch {
            // Get the current application instance
            val application = _application.value
            if (application != null) {
                // Check if a new version of the application is available for download
                if (application.version_code > BuildConfig.VERSION_CODE
                    && application.download_url.isNotEmpty()
                    && _progress.value == 0
                ) {
                    // Download the latest version of the application
                    download(application.download_url)
                    _progress.value = 1
                }
            } else {
                // Get the latest application instance from the server
                httpCall {
                    it.find { app -> app.application_id == BuildConfig.APPLICATION_ID }
                }
            }
        }
    }

    /**
     * Downloads an APK file from the specified URL and installs it on the device.
     *
     * @param url The URL of the APK file to download.
     */
    private fun download(url: String) {
        viewModelScope.launch {
            // Download the APK file and update the progress state
            url.download(File(Ext.ctx.getExternalFilesDir(null), "update.apk"))
                .collect {
                    when (it) {
                        is DownloadState.Success -> {
                            // Install the APK file and reset the progress state
                            _progress.value = 0
                            Ext.ctx.installApk(it.file)
                        }

                        is DownloadState.Err -> {
                            // Reset the progress state and display an error message
                            _progress.value = 0
                            Ext.ctx.getString(R.string.download_failed).showShortToast()
                        }

                        is DownloadState.Progress -> {
                            // Update the progress state
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