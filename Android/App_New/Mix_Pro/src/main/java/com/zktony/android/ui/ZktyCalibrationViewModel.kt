package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.logic.data.dao.CalibrationDao
import com.zktony.android.logic.data.entities.CalibrationData
import com.zktony.android.logic.data.entities.CalibrationEntity
import com.zktony.android.logic.ext.syncTx
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/9 13:19
 */
class ZktyCalibrationViewModel constructor(
    private val dao: CalibrationDao,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalibrationUiState())
    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.LIST)
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

    fun event(event: CalibrationEvent) {
        when (event) {
            is CalibrationEvent.NavTo -> _page.value = event.page
            is CalibrationEvent.ToggleSelected -> _selected.value = event.id
            is CalibrationEvent.Insert -> async { dao.insert(CalibrationEntity(text = event.name)) }
            is CalibrationEvent.Delete -> async { dao.deleteById(event.id) }
            is CalibrationEvent.Update -> async { dao.update(event.entity) }
            is CalibrationEvent.Active -> async { dao.active(event.id) }
            is CalibrationEvent.AddLiquid -> addLiquid(event.index)
            is CalibrationEvent.DeleteData -> deleteData(event.data)
            is CalibrationEvent.InsertData -> insertData(event.index, event.volume)
        }
    }

    private fun async(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }

    private fun addLiquid(index: Int) {
        viewModelScope.launch {
            syncTx {
                when (index) {
                    0 -> pulse {
                        this.index = 3
                        pulse = 3200L * 30
                    }

                    1 -> pulse {
                        this.index = 4
                        pulse = 3200L * 30
                    }
                }
            }
        }
    }

    private fun deleteData(data: CalibrationData) {
        viewModelScope.launch {
            val entity = _uiState.value.entities.find { it.id == _uiState.value.selected }
            if (entity != null) {
                dao.update(entity.copy(data = entity.data - data))
            }
        }
    }

    private fun insertData(index: Int, volume: Double) {
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
    val page: PageType = PageType.LIST
)

sealed class CalibrationEvent {
    data class NavTo(val page: PageType) : CalibrationEvent()
    data class ToggleSelected(val id: Long) : CalibrationEvent()
    data class Insert(val name: String) : CalibrationEvent()
    data class Delete(val id: Long) : CalibrationEvent()
    data class Update(val entity: CalibrationEntity) : CalibrationEvent()
    data class Active(val id: Long) : CalibrationEvent()
    data class AddLiquid(val index: Int) : CalibrationEvent()
    data class DeleteData(val data: CalibrationData) : CalibrationEvent()
    data class InsertData(val index: Int, val volume: Double) : CalibrationEvent()
}