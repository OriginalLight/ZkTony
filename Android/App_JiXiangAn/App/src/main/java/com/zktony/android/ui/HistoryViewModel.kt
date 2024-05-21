package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.ui.utils.PageType
import com.zktony.room.dao.HistoryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    val page = _page.asStateFlow()
    val selected = _selected.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40)
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    fun dispatch(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.Delete -> viewModelScope.launch { dao.deleteById(intent.id) }
            is HistoryIntent.NavTo -> _page.value = intent.page
            is HistoryIntent.Selected -> _selected.value = intent.id
        }
    }

}

sealed class HistoryIntent {
    data class NavTo(val page: PageType) : HistoryIntent()
    data class Delete(val id: Long) : HistoryIntent()
    data class Selected(val id: Long) : HistoryIntent()
}

