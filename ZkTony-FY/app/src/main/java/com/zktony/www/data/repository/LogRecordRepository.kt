package com.zktony.www.data.repository

import com.zktony.www.common.room.dao.LogDao
import com.zktony.www.common.room.entity.Log
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 8:52
 */
class LogRecordRepository @Inject constructor(
    private val logDao: LogDao
) {
    suspend fun insert(logRecord: Log) {
        logDao.insert(logRecord)
    }

    suspend fun update(logRecord: Log) {
        logDao.update(logRecord)
    }

    suspend fun updateBatch(logRecordList: List<Log>) {
        logDao.updateBatch(logRecordList)
    }

    suspend fun deleteByDate() {
        logDao.deleteByDate()
    }

    fun withoutUpload(): Flow<List<Log>> {
        return logDao.withoutUpload()
    }

    fun getByDate(start: Date, end: Date): Flow<List<Log>> {
        return logDao.getByDate(start, end)
    }
}