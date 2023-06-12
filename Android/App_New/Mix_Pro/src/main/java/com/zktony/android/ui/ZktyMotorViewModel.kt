package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.logic.data.dao.MotorDao
import com.zktony.android.logic.data.entities.MotorEntity
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 */
class ZktyMotorViewModel constructor(
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

    fun navTo(page: PageType) {
        _page.value = page
    }

    fun toggleSelected(id: Long) {
        _selected.value = id
    }

    fun update(entity: MotorEntity) {
        viewModelScope.launch {
            dao.update(entity)
        }
    }
}

data class MotorUiState(
    val entities: List<MotorEntity> = emptyList(),
    val selected: Long = 0L,
    val page: PageType = PageType.LIST,
)