package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.entities.MotorEntity
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 */
class MotorViewModel constructor(
    private val dao: MotorDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(MotorUiState())
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
                MotorUiState(entities = entities, selected = selected, page = page)
            }.catch { ex ->
                ex.printStackTrace()
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun event(event: MotorEvent) {
        when (event) {
            is MotorEvent.NavTo -> _page.value = event.page
            is MotorEvent.ToggleSelected -> _selected.value = event.id
            is MotorEvent.Update -> async { dao.update(event.entity) }
        }
    }

    private fun async(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }
}

data class MotorUiState(
    val entities: List<MotorEntity> = emptyList(),
    val selected: Long = 0L,
    val page: PageType = PageType.LIST,
)

sealed class MotorEvent {
    data class NavTo(val page: PageType) : MotorEvent()
    data class ToggleSelected(val id: Long) : MotorEvent()
    data class Update(val entity: MotorEntity) : MotorEvent()
}