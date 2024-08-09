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
        // 只保留最新的10000条错误日志
        val effect = errorLogDao.deleteOutOf(10000)
        android.util.Log.d("ErrorLogRepository", "clearExpiredLog: $effect")
    }
}