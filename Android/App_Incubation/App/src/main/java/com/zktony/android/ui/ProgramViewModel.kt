package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author 刘贺贺
 * @date 2023/5/15 14:51
 */
@HiltViewModel
class ProgramViewModel @Inject constructor(
    private val dao: ProgramDao
) : ViewModel() {

    private val _page = MutableStateFlow(PageType.PROGRAM_LIST)
    private val _selected = MutableStateFlow(0L)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())

    val page = _page.asStateFlow()
    val selected = _selected.asStateFlow()
    val uiFlags = _uiFlags.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40)
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)


    fun dispatch(intent: ProgramIntent) {
        when (intent) {
            is ProgramIntent.Delete -> viewModelScope.launch { dao.deleteById(intent.id) }
            is ProgramIntent.Insert -> viewModelScope.launch { dao.insert(intent.program) }
            is ProgramIntent.Flags -> _uiFlags.value = intent.uiFlags
            is ProgramIntent.NavTo -> _page.value = intent.page
            is ProgramIntent.Selected -> _selected.value = intent.id
            is ProgramIntent.Update -> viewModelScope.launch { dao.update(intent.program) }
        }
    }
}

sealed class ProgramIntent {
    data class Delete(val id: Long) : ProgramIntent()
    data class Insert(val program: Program) : ProgramIntent()
    data class Flags(val uiFlags: UiFlags): ProgramIntent()
    data class NavTo(val page: Int) : ProgramIntent()
    data class Selected(val id: Long) : ProgramIntent()
    data class Update(val program: Program) : ProgramIntent()
}