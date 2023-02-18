package com.zktony.www.ui.log

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.data.local.room.entity.LogData
import com.zktony.www.data.repository.LogDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogChartViewModel @Inject constructor(
    private val logDataRepository: LogDataRepository
) : BaseViewModel() {

    private val _logList = MutableStateFlow(emptyList<LogData>())
    val logList = _logList.asStateFlow()

    fun loadData(id: String) {
        viewModelScope.launch {
            logDataRepository.getByLogId(id).collect {
                _logList.value = it
            }
        }
    }
}