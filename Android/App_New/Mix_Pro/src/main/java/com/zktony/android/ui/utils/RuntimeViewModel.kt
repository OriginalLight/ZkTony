package com.zktony.android.ui.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.logic.data.dao.ProgramDao
import com.zktony.android.logic.data.entities.ProgramEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class RunTimeViewModel constructor(
    private val dao: ProgramDao,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    private val _page = MutableStateFlow(PageType.LIST)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dao.getAll(),
                _page
            ) { entities, page ->
                HomeUiState(
                    entities = entities,
                    page = page
                )
            }.catch { ex ->
                ex.printStackTrace()
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun navTo(page: PageType) {
        _page.value = page
    }

}

data class HomeUiState(
    val entities: List<ProgramEntity> = emptyList(),
    val page: PageType = PageType.LIST,
)