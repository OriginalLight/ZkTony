package com.zktony.android.ui.screen.calibration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.entity.Calibration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/9 13:19
 */
class CalibrationViewModel constructor(
    private val dao: CalibrationDao,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalibrationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().collect {
                _uiState.value = _uiState.value.copy(list = it)
            }
        }
    }

    /**
     * Navigate to
     *
     * @param page CalibrationPage
     * @return Unit
     */
    fun navigateTo(page: CalibrationPageEnum) {
        _uiState.value = _uiState.value.copy(page = page)
    }

    /**
     * toggleEntity
     *
     * @param calibration Calibration
     * @return Unit
     */
    fun toggleEntity(calibration: Calibration?) {
        _uiState.value = _uiState.value.copy(entity = calibration)
    }

    /**
     * Insert
     *
     * @param name String
     * @return Unit
     */
    fun insert(name: String) {
        viewModelScope.launch {
            dao.insert(Calibration(name = name))
        }
    }

    /**
     * Delete
     *
     * @param id Long
     * @return Unit
     */
    fun delete(calibration: Calibration) {
        viewModelScope.launch {
            dao.delete(calibration)
            delay(500L)
            val list = uiState.value.list
            if (list.isNotEmpty() && !list.any { it.active == 1 }) {
                dao.update(list[0].copy(active = 1))
            }
        }
    }


    /**
     * Update
     *
     * @param calibration Calibration
     * @return Unit
     */
    fun update(calibration: Calibration) {
        viewModelScope.launch {
            dao.update(calibration)
            _uiState.value = _uiState.value.copy(entity = calibration)
        }
    }

    /**
     * 加液
     *
     * @param i Int
     * @param fl Float
     * @return Unit
     */
    fun addLiquid(i: Int, fl: Float) {

    }

    fun activeEntity(calibration: Calibration) {
        viewModelScope.launch {
            val list = uiState.value.list
                .map {
                    if (it.id != calibration.id) {
                        it.copy(active = 0)
                    } else {
                        it.copy(active = 1)
                    }
                }
            dao.updateAll(list)
        }
    }

}

data class CalibrationUiState(
    val list: List<Calibration> = emptyList(),
    val entity: Calibration? = null,
    val page: CalibrationPageEnum = CalibrationPageEnum.CALIBRATION,
)

enum class CalibrationPageEnum {
    CALIBRATION,
    CALIBRATION_ADD,
    CALIBRATION_EDIT
}