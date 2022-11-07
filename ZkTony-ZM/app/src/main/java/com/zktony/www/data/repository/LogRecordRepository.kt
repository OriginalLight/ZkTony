package com.zktony.www.data.repository

import com.zktony.www.common.room.dao.LogRecordDao
import com.zktony.www.common.room.entity.LogRecord
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 8:52
 */
class LogRecordRepository @Inject constructor(
    private val dao: LogRecordDao
) {
    suspend fun insert(logRecord: LogRecord) {
        dao.insert(logRecord)
    }

    suspend fun update(logRecord: LogRecord) {
        dao.update(logRecord)
    }

    suspend fun updateBatch(logRecordList: List<LogRecord>) {
        dao.updateBatch(logRecordList)
    }

    suspend fun deleteByDate() {
        dao.deleteByDate()
    }

    suspend fun deleteInvalidedLog() {
        dao.deleteInvaliedLog()
    }

    fun withoutUpload(): Flow<List<LogRecord>> {
        return dao.withoutUpload()
    }

    fun getByDate(start: Date, end: Date): Flow<List<LogRecord>> {
        return dao.getByDate(start, end)
    }

    fun getAll(): Flow<List<LogRecord>> {
        return dao.getAll()
    }
}