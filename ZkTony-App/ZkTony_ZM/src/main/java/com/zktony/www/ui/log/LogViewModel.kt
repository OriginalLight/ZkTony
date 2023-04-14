package com.zktony.www.ui.log

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.getDayEnd
import com.zktony.core.ext.getDayStart
import com.zktony.www.room.dao.LogDataDao
import com.zktony.www.room.dao.LogRecordDao
import com.zktony.www.room.entity.LogRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class LogViewModel constructor(
    private val LRD: LogRecordDao,
    private val LDD: LogDataDao
) : BaseViewModel() {

    private val _logList = MutableStateFlow(emptyList<LogRecord>())
    val logList = _logList.asStateFlow()

    /**
     * 获取所有记录
     */
    init {
        viewModelScope.launch {
            LRD.getAll().collect {
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
            LRD.getByDate(start.getDayStart(), end.getDayEnd())
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
            LRD.delete(logRecord)
            LDD.deleteByRecordId(logRecord.id)
        }
    }
}