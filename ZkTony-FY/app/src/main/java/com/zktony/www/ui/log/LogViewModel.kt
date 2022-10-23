package com.zktony.www.ui.log

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.extension.getDayEnd
import com.zktony.www.common.extension.getDayStart
import com.zktony.www.data.repository.LogDataRepository
import com.zktony.www.data.repository.LogRecordRespository
import com.zktony.www.ui.log.model.LogIntent
import com.zktony.www.ui.log.model.LogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val logRecordRepository: LogRecordRespository,
    private val logDataRepository: LogDataRepository
) : BaseViewModel() {

    private val _state = MutableSharedFlow<LogState>()
    val state: SharedFlow<LogState> get() = _state
    private val intent = MutableSharedFlow<LogIntent>()

    init {
        viewModelScope.launch {
            intent.collect {
                when (it) {
                    is LogIntent.ChangeLogRecord -> changeLogRecord(it.start, it.end)
                    is LogIntent.ChangeLogData -> changeLogData(it.id)
                }
            }
        }
    }

    fun dispatch(intent: LogIntent) {
        try {
            viewModelScope.launch {
                this@LogViewModel.intent.emit(intent)
            }
        } catch (_: Exception) {
        }

    }


    private fun changeLogRecord(start: Date, end: Date) {
        viewModelScope.launch {
            logRecordRepository.getByDate(start.getDayStart(), end.getDayEnd())
                .collect {
                    _state.emit(LogState.ChangeLogRecord(it))
                }
        }
    }

    private fun changeLogData(id: String) {
        viewModelScope.launch {
            logDataRepository.getByLogId(id).collect {
                _state.emit(LogState.ChangeLogData(it))
            }
        }
    }
}