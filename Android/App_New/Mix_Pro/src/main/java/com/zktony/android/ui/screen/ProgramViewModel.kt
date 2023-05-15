package com.zktony.android.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entity.ProgramEntity
import com.zktony.android.ui.navigation.PageEnum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/15 14:51
 */
class ProgramViewModel constructor(
    private val dao: ProgramDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProgramUiState())
    private val _page = MutableStateFlow(PageEnum.MAIN)
    private val _selected = MutableStateFlow(0L)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dao.getAll(),
                _page,
                _selected,
            ) { entities, page, selectedId ->
                ProgramUiState(entities = entities, page = page, selected = selectedId)
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
            dao.insert(ProgramEntity(name = name))
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

}

data class ProgramUiState(
    val entities: List<ProgramEntity> = emptyList(),
    val selected: Long = 0L,
    val page: PageEnum = PageEnum.MAIN,
)