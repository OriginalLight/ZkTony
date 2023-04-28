package com.zktony.www.ui.log

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.*
import com.zktony.www.room.dao.LogDao
import com.zktony.www.room.entity.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class LogViewModel constructor(
    private val LD: LogDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * 获取所有记录
     */
    init {
        viewModelScope.launch {
            LD.getAll().collect {
                _uiState.value = _uiState.value.copy(list = it)
            }
        }
    }

    /**
     * 获取日志记录
     */
    fun search() {
        viewModelScope.launch {
            val start = _uiState.value.startTime
            val end = _uiState.value.endTime
            LD.getByDate(start.getDayStart(), end.getDayEnd())
                .collect {
                    _uiState.value = _uiState.value.copy(list = it)
                    if (it.isEmpty()) {
                        PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.not_found))
                    }
                }
        }
    }

    /**
     * 删除日志记录
     */
    fun delete(entity: Log) {
        viewModelScope.launch {
            LD.delete(entity)
        }
    }

    /**
     * 切换搜索栏
     */
    fun showSearchBar() {
        val search = _uiState.value.bar
        _uiState.value = _uiState.value.copy(bar = !search)
    }

    fun setStartTime(date: Date) {
        _uiState.value = _uiState.value.copy(startTime = date)
    }

    fun setEndTime(date: Date) {
        _uiState.value = _uiState.value.copy(endTime = date)
    }
}

data class LogUiState(
    val list: List<Log> = emptyList(),
    val bar: Boolean = false,
    val startTime: Date = Date(System.currentTimeMillis()),
    val endTime: Date = Date(System.currentTimeMillis())
)
