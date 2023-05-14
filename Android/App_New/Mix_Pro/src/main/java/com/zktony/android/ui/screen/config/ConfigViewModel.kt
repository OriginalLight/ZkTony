package com.zktony.android.ui.screen.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.ui.navigation.PageEnum
import com.zktony.datastore.SettingsPreferences
import com.zktony.datastore.copy
import com.zktony.datastore.ext.saveSettings
import com.zktony.datastore.ext.settingsFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class ConfigViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ConfigUiState())
    private val _page = MutableStateFlow(PageEnum.MAIN)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsFlow,
                _page,
            ) { settings, page ->
                ConfigUiState(settings = settings, page = page)
            }.catch { ex ->
                _uiState.value = ConfigUiState(errorMessage = ex.message ?: "Unknown error")
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun navigationTo(page: PageEnum) {
        _page.value = page
    }

    fun setTravel(x: Float, y: Float, z: Float) {
        viewModelScope.launch {
            saveSettings {
                it.copy {
                    travel.clear()
                    travel.addAll(listOf(x, y, z))
                }
            }
        }
    }

    fun setWaste(x: Float, y: Float, z: Float) {
        viewModelScope.launch {
            saveSettings {
                it.copy {
                    waste.clear()
                    waste.addAll(listOf(x, y, z))
                }
            }
        }
    }

}

data class ConfigUiState(
    val settings: SettingsPreferences = SettingsPreferences.getDefaultInstance(),
    val page: PageEnum = PageEnum.MAIN,
    val errorMessage: String = "",
)