package com.zktony.www.ui.log

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.extension.getDayEnd
import com.zktony.www.common.extension.getDayStart
import com.zktony.www.data.model.LogRecord
import com.zktony.www.data.repository.LogDataRepository
import com.zktony.www.data.repository.LogRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val logRecordRepository: LogRecordRepository,
    private val logDataRepository: LogDataRepository
) : BaseViewModel() {

    private val _logList = MutableStateFlow(emptyList<LogRecord>())
    val logList = _logList.asStateFlow()

    /**
     * 获取所有记录
     */
    fun initLogRecord() {
        viewModelScope.launch {
            logRecordRepository.getAll()
                .collect {
                    _logList.value = it
                }
        }
    }

    /**
     * 获取日志记录
     * @param start 开始时间
     * @param end 结束时间
     */
    fun changeLogRecord(start: Date, end: Date) {
        viewModelScope.launch {
            logRecordRepository.getByDate(start.getDayStart(), end.getDayEnd())
                .collect {
                    _logList.value = it
                }
        }
    }

    /**
     * 删除日志记录
     * @param logRecord 日志记录
     */
    fun delete(logRecord: LogRecord) {
        viewModelScope.launch {
            logRecordRepository.delete(logRecord)
            logDataRepository.deleteByRecordId(logRecord.id)
        }
    }
}