package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
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
class DebugModeViewModel @Inject constructor(
) : ViewModel() {

    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.DEBUGMODE)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())

    val selected = _selected.asStateFlow()
    val page = _page.asStateFlow()
    val uiFlags = _uiFlags.asStateFlow()

    fun dispatch(intent: DebugModeIntent) {
        when (intent) {
            is DebugModeIntent.NavTo -> _page.value = intent.page
            is DebugModeIntent.Flags -> _uiFlags.value = intent.uiFlags
            is DebugModeIntent.Selected -> _selected.value = intent.id

            else -> {}
        }
    }
}


sealed class DebugModeIntent {
    data class NavTo(val page: Int) : DebugModeIntent()
    data class Flags(val uiFlags: UiFlags) : DebugModeIntent()
    data class Selected(val id: Long) : DebugModeIntent()
    data class Insert(
        val name: String,
        val startRange: Double,
        val endRange: Double,
        val thickness: String,
        val coagulant: Double,
        val volume: Double,
        val founder: String
    ) : DebugModeIntent()

    data class Delete(val id: Long) : DebugModeIntent()
}