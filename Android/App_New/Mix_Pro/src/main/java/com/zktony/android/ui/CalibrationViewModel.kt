package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.entities.CalibrationData
import com.zktony.android.data.entities.CalibrationEntity
import com.zktony.android.core.dsl.tx
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
class CalibrationViewModel constructor(
    private val dao: CalibrationDao,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalibrationUiState())
    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.LIST)
    private val _loading = MutableStateFlow(false)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dao.getAll(),
                _selected,
                _page,
                _loading,
            ) { entities, selected, page, loading ->
                CalibrationUiState(
                    entities = entities,
                    selected = selected,
                    page = page,
                    loading = loading
                )
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
            _loading.value = true
            tx {
                mpm {
                    this.index = index + 2
                    pulse = 3200L * 20
                }
            }
            _loading.value = false
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
                            pulse = 3200 * 20,
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
    val page: PageType = PageType.LIST,
    val loading: Boolean = false,
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