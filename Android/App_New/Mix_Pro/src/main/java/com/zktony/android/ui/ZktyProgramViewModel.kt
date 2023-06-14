package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.logic.data.dao.ProgramDao
import com.zktony.android.logic.data.entities.ProgramEntity
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
class ZktyProgramViewModel constructor(
    private val dao: ProgramDao,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProgramUiState())
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
                ProgramUiState(entities = entities, selected = selected, page = page)
            }.catch { ex ->
                ex.printStackTrace()
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun event(event: ProgramEvent) {
        when (event) {
            is ProgramEvent.NavTo -> _page.value = event.page
            is ProgramEvent.ToggleSelected -> _selected.value = event.id
            is ProgramEvent.Insert -> async { dao.insert(ProgramEntity(text = event.name)) }
            is ProgramEvent.Update -> async { dao.update(event.entity) }
            is ProgramEvent.Delete -> async { dao.deleteById(event.id) }
        }
    }

    private fun async(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }
}

data class ProgramUiState(
    val entities: List<ProgramEntity> = emptyList(),
    val selected: Long = 0L,
    val page: PageType = PageType.LIST,
)

sealed class ProgramEvent {
    data class NavTo(val page: PageType) : ProgramEvent()
    data class ToggleSelected(val id: Long) : ProgramEvent()
    data class Insert(val name: String) : ProgramEvent()
    data class Update(val entity: ProgramEntity) : ProgramEvent()
    data class Delete(val id: Long) : ProgramEvent()
}