package com.zktony.android.ui.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.datastore.ext.read
import com.zktony.datastore.ext.save
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class ConfigViewModel constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val _uiState = MutableStateFlow(ConfigUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                dataStore.read("X_AXIS_TRAVEL", 0f).collect {
                    _uiState.value = _uiState.value.copy(xAxisTravel = it)
                }
            }
            launch {
                dataStore.read("Y_AXIS_TRAVEL", 0f).collect {
                    _uiState.value = _uiState.value.copy(yAxisTravel = it)
                }
            }
            launch {
                dataStore.read("Z_AXIS_TRAVEL", 0f).collect {
                    _uiState.value = _uiState.value.copy(zAxisTravel = it)
                }
            }
            launch {
                dataStore.read("WASTE_X", 0f).collect {
                    _uiState.value = _uiState.value.copy(wasteX = it)
                }
            }
            launch {
                dataStore.read("WASTE_Y", 0f).collect {
                    _uiState.value = _uiState.value.copy(wasteY = it)
                }
            }
            launch {
                dataStore.read("WASTE_Z", 0f).collect {
                    _uiState.value = _uiState.value.copy(wasteZ = it)
                }
            }
        }
    }

    /**
     * 本页面跳转
     *
     * @param page ConfigPage
     * @return Unit
     */
    fun navigateTo(page: ConfigPage) {
        _uiState.value = _uiState.value.copy(page = page)
    }

    /**
     * 设置行程
     *
     * @param x Float
     * @param y Float
     * @param z Float
     * @return Unit
     */
    fun setTravel(x: Float, y: Float, z: Float) {
        dataStore.save("X_AXIS_TRAVEL", x)
        dataStore.save("Y_AXIS_TRAVEL", y)
        dataStore.save("Z_AXIS_TRAVEL", z)
    }

    /**
     * 设置废液槽坐标
     *
     * @param x Float
     * @param y Float
     * @param z Float
     * @return Unit
     */
    fun setWaste(x: Float, y: Float, z: Float) {
        dataStore.save("WASTE_X", x)
        dataStore.save("WASTE_Y", y)
        dataStore.save("WASTE_Z", z)
    }

}

data class ConfigUiState(
    val xAxisTravel: Float = 0f,
    val yAxisTravel: Float = 0f,
    val zAxisTravel: Float = 0f,
    val wasteX: Float = 0f,
    val wasteY: Float = 0f,
    val wasteZ: Float = 0f,
    val page: ConfigPage = ConfigPage.CONFIG,
)

enum class ConfigPage {
    CONFIG,
    TRAVEL_EDIT,
    WASTE_EDIT,
}
