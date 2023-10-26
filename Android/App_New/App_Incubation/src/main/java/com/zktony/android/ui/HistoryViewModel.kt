package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.dao.HistoryDao
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
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
class HistoryViewModel @Inject constructor(
    private val dao: HistoryDao
) : ViewModel() {

    private val _page = MutableStateFlow(PageType.HISTORY_LIST)
    private val _selected = MutableStateFlow(0L)
    private val _uiFlags = MutableStateFlow(UiFlags.NONE)
    private val _uiState = MutableStateFlow(HistoryUiState())
    private val _message = MutableStateFlow<String?>(null)

    val uiState = _uiState.asStateFlow()
    val message = _message.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40)
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            combine(_selected, _page, _uiFlags) { selected, page, loading ->
                HistoryUiState(selected, page, loading)
            }.catch {
                _message.value = it.message
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun uiEvent(uiEvent: HistoryUiEvent) {
        when (uiEvent) {
            is HistoryUiEvent.Delete -> viewModelScope.launch { dao.deleteById(uiEvent.id) }
            is HistoryUiEvent.Message -> _message.value = uiEvent.message
            is HistoryUiEvent.NavTo -> _page.value = uiEvent.page
            is HistoryUiEvent.ToggleSelected -> _selected.value = uiEvent.id
        }
    }

}

data class HistoryUiState(
    val selected: Long = 0L,
    val page: Int = PageType.HISTORY_LIST,
    val uiFlags: Int = UiFlags.NONE
)

sealed class HistoryUiEvent {
    data class Delete(val id: Long) : HistoryUiEvent()
    data class Message(val message: String?) : HistoryUiEvent()
    data class NavTo(val page: Int) : HistoryUiEvent()
    data class ToggleSelected(val id: Long) : HistoryUiEvent()
}

