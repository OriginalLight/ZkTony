package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.entities.Calibration
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.SerialPortUtils.start
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())

    val page = _page.asStateFlow()
    val selected = _selected.asStateFlow()
    val uiFlags = _uiFlags.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    fun dispatch(intent: CalibrationIntent) {
        when (intent) {
            is CalibrationIntent.NavTo -> _page.value = intent.page
            is CalibrationIntent.Selected -> _selected.value = intent.id
            is CalibrationIntent.Insert -> viewModelScope.launch {
                dao.insert(
                    Calibration(
                        displayText = intent.displayText
                    )
                )
            }

            is CalibrationIntent.Delete -> viewModelScope.launch { dao.deleteById(intent.id) }
            is CalibrationIntent.Update -> viewModelScope.launch { dao.update(intent.calibration) }
            is CalibrationIntent.Transfer -> transfer(intent.index, intent.turns)
            is CalibrationIntent.Flags -> _uiFlags.value = intent.uiFlags
        }
    }

    private fun transfer(index: Int, turns: Double) {
        viewModelScope.launch {
            _uiFlags.value = UiFlags.loading()
            start { with(index, (turns * 3200L).toLong()) }
            _uiFlags.value = UiFlags.none()
        }
    }
}

sealed class CalibrationIntent {
    data class Transfer(val index: Int, val turns: Double) : CalibrationIntent()
    data class Delete(val id: Long) : CalibrationIntent()
    data class Insert(val displayText: String) : CalibrationIntent()
    data class NavTo(val page: Int) : CalibrationIntent()
    data class Selected(val id: Long) : CalibrationIntent()
    data class Update(val calibration: Calibration) : CalibrationIntent()
    data class Flags(val uiFlags: UiFlags) : CalibrationIntent()
}