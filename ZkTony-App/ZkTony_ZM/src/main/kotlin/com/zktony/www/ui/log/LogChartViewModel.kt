package com.zktony.www.ui.log

import androidx.lifecycle.viewModelScope
import com.zktony.common.base.BaseViewModel
import com.zktony.www.data.local.room.dao.LogDataDao
import com.zktony.www.data.local.room.entity.LogData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogChartViewModel @Inject constructor(
    private val logDataDao: LogDataDao
) : BaseViewModel() {

    private val _logList = MutableStateFlow(emptyList<LogData>())
    val logList = _logList.asStateFlow()

    fun loadData(id: String) {
        viewModelScope.launch {
            logDataDao.getByLogId(id).collect {
                _logList.value = it
            }
        }
    }
}