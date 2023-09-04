package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.entities.Calibration
import com.zktony.android.ui.utils.PageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author 刘贺贺
 * @date 2023/5/9 13:19
 */
@HiltViewModel
class CalibrationViewModel @Inject constructor(
    private val dao: CalibrationDao
) : ViewModel() {

    private val _page = MutableStateFlow(PageType.CALIBRATION_LIST)
    private val _selected = MutableStateFlow(0L)
    private val _uiFlags = MutableStateFlow(0)
    private val _uiState = MutableStateFlow(CalibrationUiState())
    private val _message = MutableStateFlow<String?>(null)

    val uiState = _uiState.asStateFlow()
    val message = _message.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            combine(_selected, _page, _uiFlags) { selected, page, uiFlags ->
                CalibrationUiState(selected, page, uiFlags)
            }.catch { ex ->
                _message.value = ex.message
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun uiEvent(uiEvent: CalibrationUiEvent) {
        when (uiEvent) {
            is CalibrationUiEvent.NavTo -> _page.value = uiEvent.page
            is CalibrationUiEvent.ToggleSelected -> _selected.value = uiEvent.id
            is CalibrationUiEvent.Insert -> viewModelScope.launch {
                dao.insert(
                    Calibration(
                        displayText = uiEvent.displayText
                    )
                )
            }

            is CalibrationUiEvent.Delete -> viewModelScope.launch { dao.deleteById(uiEvent.id) }
            is CalibrationUiEvent.Update -> viewModelScope.launch { dao.update(uiEvent.calibration) }
        }
    }
}

data class CalibrationUiState(
    val selected: Long = 0L,
    val page: PageType = PageType.CALIBRATION_LIST,
    val uiFlags: Int = 0,
)

sealed class CalibrationUiEvent {
    data class NavTo(val page: PageType) : CalibrationUiEvent()
    data class ToggleSelected(val id: Long) : CalibrationUiEvent()
    data class Insert(val displayText: String) : CalibrationUiEvent()
    data class Delete(val id: Long) : CalibrationUiEvent()
    data class Update(val calibration: Calibration) : CalibrationUiEvent()
}