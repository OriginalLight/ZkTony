package com.zktony.www.data.repository

import com.zktony.www.data.dao.LogRecordDao
import com.zktony.www.data.entity.LogRecord
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 8:52
 */
class LogRecordRespository @Inject constructor(
    private val logRecordDao: LogRecordDao
) {
    suspend fun insert(logRecord: LogRecord) {
        logRecordDao.insert(logRecord)
    }

    suspend fun update(logRecord: LogRecord) {
        logRecordDao.update(logRecord)
    }

    suspend fun updateBatch(logRecordList: List<LogRecord>) {
        logRecordDao.updateBatch(logRecordList)
    }

    suspend fun deleteByDate() {
        logRecordDao.deleteByDate()
    }

    suspend fun deleteInvaliedLog() {
        logRecordDao.deleteInvaliedLog()
    }

    fun withoutUpload(): Flow<List<LogRecord>> {
        return logRecordDao.withoutUpload()
    }

    fun getByDate(start: Date, end: Date): Flow<List<LogRecord>> {
        return logRecordDao.getByDate(start, end)
    }
}