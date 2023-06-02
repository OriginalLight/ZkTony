package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.logic.data.dao.ContainerDao
import com.zktony.android.logic.data.entities.ContainerEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/11 14:48
 */
class ZktyContainerViewModel constructor(
    private val dao: ContainerDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContainerUiState())
    private val _selected = MutableStateFlow(0L)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dao.getAll(),
                _selected,
            ) { entities, selected ->
                ContainerUiState(entities = entities, selected = selected)
            }.catch { ex ->
                ex.printStackTrace()
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun toggleSelected(id: Long) {
        _selected.value = id
    }

    fun insert(name: String) {
        viewModelScope.launch {
            dao.insert(ContainerEntity(text = name))
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    fun update(containerEntity: ContainerEntity) {
        viewModelScope.launch {
            dao.update(containerEntity)
        }
    }
}

data class ContainerUiState(
    val entities: List<ContainerEntity> = emptyList(),
    val selected: Long = 0L,
)