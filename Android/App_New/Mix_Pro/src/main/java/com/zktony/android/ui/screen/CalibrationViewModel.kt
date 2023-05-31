package com.zktony.android.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.logic.data.dao.CalibrationDao
import com.zktony.android.logic.data.entities.CalibrationData
import com.zktony.android.logic.data.entities.CalibrationEntity
import com.zktony.android.logic.ext.syncTransmit
import com.zktony.android.ui.utils.PageEnum
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
                ex.printStackTrace()
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
            dao.insert(CalibrationEntity(name = name))
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    fun update(entity: CalibrationEntity) {
        viewModelScope.launch {
            dao.update(entity)
        }
    }

    fun active(id: Long) {
        viewModelScope.launch {
            dao.active(id)
        }
    }

    fun addLiquid(index: Int) {
        viewModelScope.launch {
            syncTransmit {
                when (index) {
                    0 -> m3(3200L * 10)
                    1 -> m4(3200L * 10)
                }
            }
        }
    }

    fun deleteData(data: CalibrationData) {
        viewModelScope.launch {
            val entity = _uiState.value.entities.find { it.id == _uiState.value.selected }
            if (entity != null) {
                dao.update(entity.copy(data = entity.data - data))
            }
        }
    }

    fun insertData(index: Int, volume: Double) {
        viewModelScope.launch {
            val entity = _uiState.value.entities.find { it.id == _uiState.value.selected }
            if (entity != null) {
                dao.update(
                    entity.copy(
                        data = entity.data + CalibrationData(
                            index = index,
                            pulse = 3200 * 10,
                            volume = volume,
                        )
                    )
                )
            }
        }
    }
}

data class CalibrationUiState(
    val entities: List<CalibrationEntity> = emptyList(),
    val selected: Long = 0L,
    val page: PageEnum = PageEnum.MAIN,
)