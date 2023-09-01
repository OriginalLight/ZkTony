package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
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
class ProgramViewModel(private val dao: ProgramDao) : ViewModel() {
    private val _uiState = MutableStateFlow(ProgramUiState())
    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.PROGRAM_LIST)
    private val _uiFlags = MutableStateFlow(0)
    private val _message = MutableStateFlow<String?>(null)

    val uiState = _uiState.asStateFlow()
    val message = _message.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40)
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            combine(_selected, _page, _uiFlags) { selected, page, uiFlags ->
                ProgramUiState(selected, page, uiFlags)
            }.catch { ex ->
                _message.value = ex.message
            }.collect {
                _uiState.value = it
            }
        }
    }


    fun uiEvent(event: ProgramUiEvent) {
        when (event) {
            is ProgramUiEvent.NavTo -> _page.value = event.page
            is ProgramUiEvent.ToggleSelected -> _selected.value = event.id
            is ProgramUiEvent.Insert -> viewModelScope.launch { dao.insert(Program(displayText = event.name)) }
            is ProgramUiEvent.Update -> viewModelScope.launch { dao.update(event.program) }
            is ProgramUiEvent.Delete -> viewModelScope.launch { dao.deleteById(event.id) }
        }
    }

}

data class ProgramUiState(
    val selected: Long = 0L,
    val page: PageType = PageType.PROGRAM_LIST,
    val uiFlags: Int = 0
)

sealed class ProgramUiEvent {
    data class NavTo(val page: PageType) : ProgramUiEvent()
    data class ToggleSelected(val id: Long) : ProgramUiEvent()
    data class Insert(val name: String) : ProgramUiEvent()
    data class Update(val program: Program) : ProgramUiEvent()
    data class Delete(val id: Long) : ProgramUiEvent()
}