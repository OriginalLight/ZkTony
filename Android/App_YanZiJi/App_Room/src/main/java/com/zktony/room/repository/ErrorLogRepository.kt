package com.zktony.room.repository

import com.zktony.room.dao.ErrorLogDao
import com.zktony.room.entities.ErrorLog
import javax.inject.Inject

class ErrorLogRepository @Inject constructor(
    private val errorLogDao: ErrorLogDao
) {
    /**
     * Insert a new error log
     */
    suspend fun insert(errorLog: ErrorLog): Long {
        return errorLogDao.insert(errorLog)
    }

    /**
     * Insert a list of error logs
     */
    suspend fun insertAll(errorLogs: List<ErrorLog>): List<Long> {
        return errorLogDao.insertAll(errorLogs)
    }

    /**
     * Get by page
     */
    fun getByPage() = errorLogDao.getByPage()

    /**
     * Clear expired log.
     */
    suspend fun clearExpiredLog() {
        val expired = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
        val logs = errorLogDao.getBeforeTime(expired)
        errorLogDao.deleteAll(logs)
        android.util.Log.d("ErrorLogRepository", "clearExpiredLog: ${logs.size}")
    }
}