package com.zktony.www.ui

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.core.ext.getDayEnd
import com.zktony.core.ext.getDayStart
import com.zktony.www.R
import com.zktony.www.data.dao.LogDataDao
import com.zktony.www.data.dao.LogRecordDao
import com.zktony.www.data.entities.LogRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class LogViewModel constructor(
    private val LRD: LogRecordDao,
    private val LDD: LogDataDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * 获取所有记录
     */
    init {
        viewModelScope.launch {
            LRD.getAll().collect {
                _uiState.value = _uiState.value.copy(list = it)
            }
        }
    }

    /**
     * 获取日志记录
     */
    fun search(date: Date) {
        viewModelScope.launch {
            LRD.getByDate(date.getDayStart(), date.getDayEnd())
                .collect {
                    _uiState.value = _uiState.value.copy(list = it)
                    if (it.isEmpty()) {
                        PopTip.show(Ext.ctx.getString(R.string.no_log))
                    }
                }
        }
    }

    /**
     * 删除日志记录
     * @param logRecord 日志记录
     */
    fun delete(list: List<LogRecord>) {
        viewModelScope.launch {
            list.forEach {
                LRD.delete(it)
                LDD.deleteByRecordId(it.id)
            }
        }
    }

    /**
     * 切换搜索栏
     */
    fun select(list: List<LogRecord>) {
        _uiState.value = _uiState.value.copy(selected = list)
    }

}

data class LogUiState(
    val list: List<LogRecord> = emptyList(),
    val selected: List<LogRecord> = emptyList()
)