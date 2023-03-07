package com.zktony.www.data.repository

import com.zktony.www.data.local.room.dao.LogRecordDao
import com.zktony.www.data.local.room.entity.LogRecord
import kotlinx.coroutines.flow.Flow
import java.util.*
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

    suspend fun updateAll(list: List<LogRecord>) {
        dao.updateAll(list)
    }

    suspend fun delete(logRecord: LogRecord) {
        dao.delete(logRecord)
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