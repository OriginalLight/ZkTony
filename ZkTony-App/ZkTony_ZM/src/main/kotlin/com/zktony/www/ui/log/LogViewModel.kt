package com.zktony.www.ui.log

import androidx.lifecycle.viewModelScope
import com.zktony.common.base.BaseViewModel
import com.zktony.common.ext.getDayEnd
import com.zktony.common.ext.getDayStart
import com.zktony.www.data.local.room.dao.LogDataDao
import com.zktony.www.data.local.room.dao.LogRecordDao
import com.zktony.www.data.local.room.entity.LogRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class LogViewModel constructor(
    private val logRecordDao: LogRecordDao,
    private val logDataDao: LogDataDao
) : BaseViewModel() {

    private val _logList = MutableStateFlow(emptyList<LogRecord>())
    val logList = _logList.asStateFlow()

    /**
     * 获取所有记录
     */
    init {
        viewModelScope.launch {
            logRecordDao.getAll().collect {
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
            logRecordDao.getByDate(start.getDayStart(), end.getDayEnd())
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
            logRecordDao.delete(logRecord)
            logDataDao.deleteByRecordId(logRecord.id)
        }
    }
}