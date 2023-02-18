package com.zktony.www.ui.log

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.extension.getDayEnd
import com.zktony.www.common.extension.getDayStart
import com.zktony.www.common.extension.simpleDateFormat
import com.zktony.www.data.local.room.entity.Log
import com.zktony.www.data.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val logRepository: LogRepository
) : BaseViewModel() {

    private val _logList = MutableStateFlow<List<Log>>(emptyList())
    private val _data =
        MutableStateFlow(Date(System.currentTimeMillis()).simpleDateFormat("yyyy-MM-dd"))
    val logList = _logList.asStateFlow()
    val data = _data.asStateFlow()

    init {
        viewModelScope.launch {
            _data.value = Date(System.currentTimeMillis()).simpleDateFormat("yyyy-MM-dd")
            logRepository.getAll().collect {
                _logList.value = it
            }
        }
    }

    fun changeLogRecord(start: Date, end: Date) {
        viewModelScope.launch {
            _data.value = start.simpleDateFormat("yyyy-MM-dd")
            logRepository.getByDate(start.getDayStart(), end.getDayEnd())
                .first().let {
                    _logList.value = it
                }
        }
    }

}
