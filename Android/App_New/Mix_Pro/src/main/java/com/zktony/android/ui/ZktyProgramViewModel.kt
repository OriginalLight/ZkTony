package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.logic.data.dao.ProgramDao
import com.zktony.android.logic.data.entities.ProgramEntity
import com.zktony.core.ext.loge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/15 14:51
 */
class ZktyProgramViewModel constructor(
    private val dao: ProgramDao,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProgramUiState())
    private val _selected = MutableStateFlow(0L)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dao.getAll(),
                _selected,
            ) { entities, selected ->
                ProgramUiState(
                    entities = entities,
                    selected = selected
                )
            }.catch { ex ->
                ex.printStackTrace()
            }.collect {
                it.toString().loge()
                _uiState.value = it
            }
        }
    }

    fun toggleSelected(id: Long) {
        _selected.value = id
    }

    fun insert(name: String) {
        viewModelScope.launch {
            dao.insert(ProgramEntity(text = name))
        }
    }

    fun update(entity: ProgramEntity) {
        viewModelScope.launch {
            dao.update(entity)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    fun toggleActive(index: Int) {
        viewModelScope.launch {
            val entity = uiState.value.entities.find { it.id == uiState.value.selected }
            entity?.let {
                val al = it.active.toMutableList()
                if (al.contains(index)) {
                    al.remove(index)
                } else {
                    al.add(index)
                }
                dao.update(it.copy(active = al))
            }
        }
    }
}

data class ProgramUiState(
    val entities: List<ProgramEntity> = emptyList(),
    val selected: Long = 0L,
)