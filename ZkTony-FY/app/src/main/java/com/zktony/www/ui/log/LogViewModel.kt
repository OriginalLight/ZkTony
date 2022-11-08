package com.zktony.www.ui.log

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.extension.getDayEnd
import com.zktony.www.common.extension.getDayStart
import com.zktony.www.common.room.entity.Log
import com.zktony.www.data.repository.LogRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val logRecordRepository: LogRecordRepository
) : BaseViewModel() {

    private val _event = MutableSharedFlow<LogEvent>()
    val event = _event.asSharedFlow()


    fun changeLogRecord(start: Date, end: Date) {
        viewModelScope.launch {
            logRecordRepository.getByDate(start.getDayStart(), end.getDayEnd())
                .collect {
                    _event.emit(LogEvent.ChangeLogRecord(it))
                }
        }
    }

}

sealed class LogEvent {
    data class ChangeLogRecord(val logRecordList: List<Log>) : LogEvent()
}