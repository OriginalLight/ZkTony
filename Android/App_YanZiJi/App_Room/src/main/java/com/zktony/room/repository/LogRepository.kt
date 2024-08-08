package com.zktony.room.repository

import androidx.paging.PagingSource
import com.zktony.room.dao.LogDao
import com.zktony.room.dao.LogSnapshotDao
import com.zktony.room.entities.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class LogRepository @Inject constructor(
    private val logDao: LogDao,
    private val logSnapshotDao: LogSnapshotDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // 清理过期日志
        clearExpiredLog()
    }

    /**
     * Insert.
     * @param log Log.
     * @return Long.
     */
    suspend fun insert(log: Log): Long {
        return logDao.insert(log)
    }

    /**
     * Get by page.
     * @param name String?.
     * @param startTime Long?.
     * @param endTime Long?.
     * @return PagingSource<Int, Program>.
     */
    fun getByPage(
        name: String? = null,
        startTime: Long? = null,
        endTime: Long? = null
    ): PagingSource<Int, Log> {
        return logDao.getByPage(name, startTime, endTime)
    }

    /**
     * Get by id.
     * @param id Long.
     * @return Log?.
     */
    fun getById(id: Long): Log? {
        return logDao.getById(id)
    }

    /**
     * Update.
     * @param log Log.
     * @return Int.
     */
    suspend fun update(log: Log): Int {
        return logDao.update(log)
    }

    /**
     * Delete by ids.
     * @param ids List<Long>.
     * @return Boolean.
     */
    suspend fun deleteByIds(ids: List<Long>): Boolean {
        ids.forEach {
            logSnapshotDao.deleteBySubId(it)
        }
        val effect = logDao.deleteByIds(ids)
        return effect > 0
    }

    /**
     * Clear expired log.
     */
    private fun clearExpiredLog() {
        scope.launch {
            val expired = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
            val logs = logDao.getBeforeTime(expired)
            logs.forEach {
                logSnapshotDao.deleteBySubId(it.id)
            }
            logDao.deleteAll(logs)
            android.util.Log.d("LogRepository", "clearExpiredLog: ${logs.size}")
        }
    }
}