package com.zktony.android.ui

import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.core.ext.DownloadState
import com.zktony.android.core.ext.Ext
import com.zktony.android.core.ext.download
import com.zktony.android.core.ext.installApk
import com.zktony.android.core.ext.isNetworkAvailable
import com.zktony.android.core.ext.restartApp
import com.zktony.android.core.ext.showShortToast
import com.zktony.android.ui.utils.PageType
import com.zktony.datastore.ext.saveSettings
import com.zktony.datastore.ext.settingsFlow
import com.zktony.proto.Application
import com.zktony.proto.SettingsPreferences
import com.zktony.proto.copy
import com.zktony.protobuf.grpc.ApplicationGrpc
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
class SettingViewModel constructor(
    private val grpc: ApplicationGrpc,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingUiState())
    private val _application = MutableStateFlow<Application?>(null)
    private val _progress = MutableStateFlow(0)
    private val _page = MutableStateFlow(PageType.LIST)

    val uiState = _uiState.asStateFlow()

    /**
     * Initializes the setting view model and sets up the UI state.
     */
    init {
        viewModelScope.launch {
            // Combine the application, settings, progress, and page values into a single UI state
            launch {
                combine(
                    _application,
                    settingsFlow,
                    _progress,
                    _page
                ) { application, settings, progress, page ->
                    SettingUiState(
                        application = application,
                        settings = settings,
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
                if (Ext.ctx.isNetworkAvailable()) {
                    grpc.getApplication(BuildConfig.APPLICATION_ID)
                        .catch { ex ->
                            ex.printStackTrace()
                        }.collect {
                            _application.value = it
                        }
                } else {
                    _application.value = null
                }
            }
        }
    }

    /**
     * Handles the specified setting event and updates the UI state accordingly.
     *
     * @param event The setting event to handle.
     */
    fun event(event: SettingEvent) {
        when (event) {
            is SettingEvent.NavTo -> _page.value = event.page
            is SettingEvent.Language -> language(event.language)
            is SettingEvent.Navigation -> navigation(event.navigation)
            is SettingEvent.Network -> network()
            is SettingEvent.Update -> update()
        }
    }

    /**
     * Sets the language preference for the application and restarts the app if necessary.
     *
     * @param new The new language preference to set.
     */
    private fun language(new: String) {
        viewModelScope.launch {
            // Get the current language preference
            val old = _uiState.value.settings.language

            // Save the new language preference to the settings
            saveSettings { it.copy { language = new } }

            // Restart the app if the language preference has changed
            if (old != new) {
                Ext.ctx.restartApp()
            }
        }
    }

    /**
     * Toggles the navigation bar on or off.
     *
     * @param nav Whether to show or hide the navigation bar.
     */
    private fun navigation(nav: Boolean) {
        viewModelScope.launch {
            // Save the navigation setting to the preferences
            saveSettings { it.copy { navigation = nav } }

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
    private fun update() {
        viewModelScope.launch {
            // Check if the network is available
            if (Ext.ctx.isNetworkAvailable()) {
                // Get the current application instance
                val application = _application.value
                if (application != null) {
                    // Check if a new version of the application is available for download
                    if (application.versionCode > BuildConfig.VERSION_CODE
                        && application.downloadUrl.isNotEmpty()
                        && _progress.value == 0
                    ) {
                        // Download the latest version of the application
                        download(application.downloadUrl)
                        _progress.value = 1
                    }
                } else {
                    // Get the latest application instance from the server
                    grpc.getApplication(BuildConfig.APPLICATION_ID)
                        .catch { ex ->
                            ex.printStackTrace()
                            Ext.ctx.getString(R.string.interface_exception).showShortToast()
                        }.collect {
                            _application.value = it
                        }
                }
            } else {
                // Display a message if the network is unavailable
                Ext.ctx.getString(R.string.network_unavailable).showShortToast()
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

/**
 * Data class that represents the UI state of the setting screen.
 *
 * @param settings The settings preferences to display in the setting screen.
 * @param application The application to display in the setting screen.
 * @param progress The progress to display in the setting screen.
 * @param page The page type to display in the setting screen.
 */
data class SettingUiState(
    val settings: SettingsPreferences = SettingsPreferences.getDefaultInstance(),
    val application: Application? = null,
    val progress: Int = 0,
    val page: PageType = PageType.LIST,
)

/**
 * Sealed class that defines the possible events that can be triggered in the setting screen.
 */
sealed class SettingEvent {
    object Network : SettingEvent()
    object Update : SettingEvent()
    data class NavTo(val page: PageType) : SettingEvent()
    data class Language(val language: String) : SettingEvent()
    data class Navigation(val navigation: Boolean) : SettingEvent()
}