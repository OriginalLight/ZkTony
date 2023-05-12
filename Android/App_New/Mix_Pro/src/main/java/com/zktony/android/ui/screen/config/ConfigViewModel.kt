package com.zktony.android.ui.screen.config

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.datastore.ext.read
import com.zktony.datastore.ext.save
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                dataStore.read("X_AXIS_TRAVEL", 0f).combine(
                    dataStore.read("Y_AXIS_TRAVEL", 0f)
                ) { x, y ->
                    Pair(x, y)
                }.combine(
                    dataStore.read("Z_AXIS_TRAVEL", 0f)
                ) { xy, z ->
                    Triple(xy.first, xy.second, z)
                }.collect {
                    _uiState.value = _uiState.value.copy(travel = it)
                }
            }
            launch {
                dataStore.read("WASTE_X", 0f).combine(
                    dataStore.read("WASTE_Y", 0f)
                ) { x, y ->
                    Pair(x, y)
                }.combine(
                    dataStore.read("WASTE_Z", 0f)
                ) { xy, z ->
                    Triple(xy.first, xy.second, z)
                }.collect {
                    _uiState.value = _uiState.value.copy(waste = it)
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
    val travel: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
    val waste: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
    val page: ConfigPage = ConfigPage.CONFIG,
)

enum class ConfigPage {
    CONFIG,
    TRAVEL_EDIT,
    WASTE_EDIT,
}
