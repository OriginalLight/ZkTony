package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.entities.Calibration
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.tx.MoveType
import com.zktony.android.utils.tx.tx
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/9 13:19
 */
class CalibrationViewModel constructor(private val dao: CalibrationDao) : ViewModel() {

    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.CALIBRATION_LIST)
    private val _loading = MutableStateFlow(false)
    private val _uiState = MutableStateFlow(CalibrationUiState())

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

    fun uiEvent(event: CalibrationUiEvent) {
        when (event) {
            is CalibrationUiEvent.NavTo -> _page.value = event.page
            is CalibrationUiEvent.ToggleSelected -> _selected.value = event.id
            is CalibrationUiEvent.Insert -> viewModelScope.launch { dao.insert(Calibration(text = event.name)) }
            is CalibrationUiEvent.Delete -> viewModelScope.launch { dao.deleteById(event.id) }
            is CalibrationUiEvent.Update -> viewModelScope.launch { dao.update(event.entity) }
            is CalibrationUiEvent.Active -> viewModelScope.launch { dao.active(event.id) }
            is CalibrationUiEvent.AddLiquid -> addLiquid(event.index)
            is CalibrationUiEvent.DeleteData -> deleteData(event.data)
            is CalibrationUiEvent.InsertData -> insertData(event.index, event.volume)
        }
    }

    private fun addLiquid(index: Int) {
        viewModelScope.launch {
            _loading.value = true

            if (index == 0) {
                tx {
                    delay = 100L
                    valve(2 to 1)
                }
            }

            tx {
                move(MoveType.MOVE_PULSE) {
                    this.index = index + 2
                    pulse = 3200L * 20
                }
            }

            if (index == 0) {
                tx {
                    delay = 100L
                    valve(2 to 0)
                }
                tx {
                    move(MoveType.MOVE_PULSE) {
                        this.index = 2
                        pulse = 3200L * 20 * -1
                    }
                }
            }

            _loading.value = false
        }
    }

    private fun deleteData(data: Triple<Int, Double, Double>) {
        viewModelScope.launch {
            // Find the selected calibration entity
            val entity = _uiState.value.entities.find { it.id == _uiState.value.selected }

            // If the selected calibration entity exists, update it by removing the data point
            entity?.let {
                val updatedEntity = it.copy(data = it.data - data)
                dao.update(updatedEntity)
            }
        }
    }

    private fun insertData(index: Int, volume: Double) {
        viewModelScope.launch {
            // Find the selected calibration entity
            val entity = _uiState.value.entities.find { it.id == _uiState.value.selected }

            // If the selected calibration entity exists, update it with the new data point
            entity?.let {
                val updatedEntity = it.copy(
                    data = it.data + Triple(index, volume, 3200 * 20.0)
                )
                dao.update(updatedEntity)
            }
        }
    }
}

data class CalibrationUiState(
    val entities: List<Calibration> = emptyList(),
    val selected: Long = 0L,
    val page: PageType = PageType.CALIBRATION_LIST,
    val loading: Boolean = false,
)

sealed class CalibrationUiEvent {
    data class NavTo(val page: PageType) : CalibrationUiEvent()
    data class ToggleSelected(val id: Long) : CalibrationUiEvent()
    data class Insert(val name: String) : CalibrationUiEvent()
    data class Delete(val id: Long) : CalibrationUiEvent()
    data class Update(val entity: Calibration) : CalibrationUiEvent()
    data class Active(val id: Long) : CalibrationUiEvent()
    data class AddLiquid(val index: Int) : CalibrationUiEvent()
    data class DeleteData(val data: Triple<Int, Double, Double>) : CalibrationUiEvent()
    data class InsertData(val index: Int, val volume: Double) : CalibrationUiEvent()
}