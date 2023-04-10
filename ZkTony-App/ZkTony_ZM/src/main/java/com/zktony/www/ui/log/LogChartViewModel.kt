package com.zktony.www.ui.log

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.www.room.dao.LogDataDao
import com.zktony.www.room.entity.LogData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LogChartViewModel constructor(
    private val dao: LogDataDao
) : BaseViewModel() {

    private val _logList = MutableStateFlow(emptyList<LogData>())
    val logList = _logList.asStateFlow()

    fun loadData(id: String) {
        viewModelScope.launch {
            dao.getByLogId(id).collect {
                _logList.value = it
            }
        }
    }
}