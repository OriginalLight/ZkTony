package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/15 14:51
 */
class ProgramViewModel constructor(private val dao: ProgramDao) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgramUiState())
    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.PROGRAM_LIST)
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
                ProgramUiState(
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

    fun uiEvent(event: ProgramUiEvent) {
        when (event) {
            is ProgramUiEvent.NavTo -> _page.value = event.page
            is ProgramUiEvent.ToggleSelected -> _selected.value = event.id
            is ProgramUiEvent.Insert -> viewModelScope.launch { dao.insert(Program(text = event.name)) }
            is ProgramUiEvent.Update -> viewModelScope.launch { dao.update(event.entity) }
            is ProgramUiEvent.Delete -> viewModelScope.launch { dao.deleteById(event.id) }
        }
    }
}

data class ProgramUiState(
    val entities: List<Program> = emptyList(),
    val selected: Long = 0L,
    val page: PageType = PageType.PROGRAM_LIST,
    val loading: Boolean = false,
)

sealed class ProgramUiEvent {
    data class NavTo(val page: PageType) : ProgramUiEvent()
    data class ToggleSelected(val id: Long) : ProgramUiEvent()
    data class Insert(val name: String) : ProgramUiEvent()
    data class Update(val entity: Program) : ProgramUiEvent()
    data class Delete(val id: Long) : ProgramUiEvent()
}