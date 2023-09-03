package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.utils.PageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dao: ProgramDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.HOME)
    private val _uiFlags = MutableStateFlow(0)
    private val _message = MutableStateFlow<String?>(null)

    val uiState = _uiState.asStateFlow()
    val message = _message.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dao.getAll(),
                _selected,
                _page,
                _uiFlags
            ) { entities, selected, page, uiFlags ->
                HomeUiState(entities, selected, page, uiFlags)
            }.catch { ex ->
                ex.printStackTrace()
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun uiEvent(uiEvent: HomeUiEvent) {
        when (uiEvent) {
            is HomeUiEvent.NavTo -> _page.value = uiEvent.page
            is HomeUiEvent.ToggleSelected -> _selected.value = uiEvent.id
        }
    }

}

data class HomeUiState(
    val entities: List<Program> = emptyList(),
    val selected: Long = 0L,
    val page: PageType = PageType.HOME,
    val uiFlags: Int = 0,
)

sealed class HomeUiEvent {
    data class NavTo(val page: PageType) : HomeUiEvent()
    data class ToggleSelected(val id: Long) : HomeUiEvent()
}

