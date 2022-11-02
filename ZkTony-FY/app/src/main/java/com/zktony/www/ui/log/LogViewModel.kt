package com.zktony.www.ui.log

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.extension.getDayEnd
import com.zktony.www.common.extension.getDayStart
import com.zktony.www.common.room.entity.LogData
import com.zktony.www.common.room.entity.LogRecord
import com.zktony.www.data.repository.LogDataRepository
import com.zktony.www.data.repository.LogRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val logRecordRepository: LogRecordRepository,
    private val logDataRepository: LogDataRepository
) : BaseViewModel() {

    private val _event = MutableSharedFlow<LogEvent>()
    val event: SharedFlow<LogEvent> get() = _event


    fun changeLogRecord(start: Date, end: Date) {
        viewModelScope.launch {
            logRecordRepository.getByDate(start.getDayStart(), end.getDayEnd())
                .collect {
                    _event.emit(LogEvent.ChangeLogRecord(it))
                }
        }
    }

    private fun changeLogData(id: String) {
        viewModelScope.launch {
            logDataRepository.getByLogId(id).collect {
                _event.emit(LogEvent.ChangeLogData(it))
            }
        }
    }
}

sealed class LogEvent {
    data class ChangeLogRecord(val logRecordList: List<LogRecord>) : LogEvent()
    data class ChangeLogData(val logDataList: List<LogData>) : LogEvent()
}