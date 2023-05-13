package com.zktony.android.ui.screen.calibration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.entity.Calibration
import com.zktony.android.ui.navigation.PageEnum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/9 13:19
 */
class CalibrationViewModel constructor(
    private val dao: CalibrationDao,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalibrationUiState())
    private val _page = MutableStateFlow(PageEnum.MAIN)
    private val _selected = MutableStateFlow(0L)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dao.getAll(),
                _selected,
                _page,
            ) { entities, selected, page ->
                CalibrationUiState(entities = entities, selected = selected, page = page)
            }.catch { ex ->
                _uiState.value = CalibrationUiState(errorMessage = ex.message ?: "Unknown error")
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun navigationTo(page: PageEnum) {
        _page.value = page
    }

    fun toggleSelected(id: Long) {
        _selected.value = id
    }

    fun insert(name: String) {
        viewModelScope.launch {
            dao.insert(Calibration(name = name))
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    fun update(entity: Calibration) {
        viewModelScope.launch {
            dao.update(entity)
        }
    }

    fun active(id: Long) {
        viewModelScope.launch {
            dao.active(id)
        }
    }

    fun addLiquid(i: Int, fl: Float) {

    }
}

data class CalibrationUiState(
    val entities: List<Calibration> = emptyList(),
    val selected: Long = 0L,
    val page: PageEnum = PageEnum.MAIN,
    val errorMessage: String = "",
)