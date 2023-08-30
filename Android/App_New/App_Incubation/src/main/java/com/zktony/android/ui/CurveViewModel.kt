package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.CurveDao
import com.zktony.android.data.entities.Curve
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/9 13:19
 */
class CurveViewModel constructor(private val dao: CurveDao) : ViewModel() {

    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.CURVE_LIST)
    private val _uiFlags = MutableStateFlow(0)
    private val _uiState = MutableStateFlow(CurveUiState())

    private val _message = MutableStateFlow<String?>(null)

    val uiState = _uiState.asStateFlow()
    val message = _message.asStateFlow()

    init {
        viewModelScope.launch {
            combine(dao.getAll(), _selected, _page, _uiFlags) { entities, selected, page, uiFlags ->
                CurveUiState(entities, selected, page, uiFlags)
            }.catch { ex ->
                _message.value = ex.message
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun uiEvent(uiEvent: CurveUiEvent) {
        when (uiEvent) {
            is CurveUiEvent.NavTo -> _page.value = uiEvent.page
            is CurveUiEvent.ToggleSelected -> _selected.value = uiEvent.id
            is CurveUiEvent.Insert -> viewModelScope.launch { dao.insert(Curve(displayText = uiEvent.displayText)) }
            is CurveUiEvent.Delete -> viewModelScope.launch { dao.deleteById(uiEvent.id) }
            is CurveUiEvent.Update -> viewModelScope.launch { dao.update(uiEvent.curve) }
        }
    }

}

data class CurveUiState(
    val entities: List<Curve> = emptyList(),
    val selected: Long = 0L,
    val page: PageType = PageType.CURVE_LIST,
    val uiFlags: Int = 0,
)

sealed class CurveUiEvent {
    data class NavTo(val page: PageType) : CurveUiEvent()
    data class ToggleSelected(val id: Long) : CurveUiEvent()
    data class Insert(val displayText: String) : CurveUiEvent()
    data class Delete(val id: Long) : CurveUiEvent()
    data class Update(val curve: Curve) : CurveUiEvent()
}