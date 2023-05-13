package com.zktony.android.ui.screen.config

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.ui.navigation.PageEnum
import com.zktony.datastore.ext.read
import com.zktony.datastore.ext.save
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class ConfigViewModel constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val _uiState = MutableStateFlow(ConfigUiState())
    private val _page = MutableStateFlow(PageEnum.MAIN)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                travel(),
                waste(),
                _page,
            ) { travel, waste, page ->
                ConfigUiState(travel = travel, waste = waste, page = page)
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

    private fun travel() = combine(
        dataStore.read("X_AXIS_TRAVEL", 0f),
        dataStore.read("Y_AXIS_TRAVEL", 0f),
        dataStore.read("Z_AXIS_TRAVEL", 0f),
    ) { x, y, z ->
        Triple(x, y, z)
    }

    private fun waste() = combine(
        dataStore.read("WASTE_X", 0f),
        dataStore.read("WASTE_Y", 0f),
        dataStore.read("WASTE_Z", 0f),
    ) { x, y, z ->
        Triple(x, y, z)
    }

    fun setTravel(x: Float, y: Float, z: Float) {
        dataStore.save("X_AXIS_TRAVEL", x)
        dataStore.save("Y_AXIS_TRAVEL", y)
        dataStore.save("Z_AXIS_TRAVEL", z)
    }

    fun setWaste(x: Float, y: Float, z: Float) {
        dataStore.save("WASTE_X", x)
        dataStore.save("WASTE_Y", y)
        dataStore.save("WASTE_Z", z)
    }

}

data class ConfigUiState(
    val travel: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
    val waste: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
    val page: PageEnum = PageEnum.MAIN,
    val errorMessage: String = "",
)