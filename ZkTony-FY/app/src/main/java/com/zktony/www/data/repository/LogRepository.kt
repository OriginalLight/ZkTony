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
class LogRepository @Inject constructor(
    private val logDao: LogDao
) {
    suspend fun insert(log: Log) {
        logDao.insert(log)
    }

    suspend fun update(log: Log) {
        logDao.update(log)
    }

    suspend fun updateBatch(logList: List<Log>) {
        logDao.updateBatch(logList)
    }

    suspend fun deleteByDate() {
        logDao.deleteByDate()
    }

    fun withoutUpload(): Flow<List<Log>> {
        return logDao.withoutUpload()
    }

    fun getAll(): Flow<List<Log>> {
        return logDao.getAll()
    }

    fun getByDate(start: Date, end: Date): Flow<List<Log>> {
        return logDao.getByDate(start, end)
    }
}