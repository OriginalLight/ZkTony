package com.zktony.room.repository

import androidx.paging.PagingSource
import com.zktony.room.dao.LogDao
import com.zktony.room.dao.LogSnapshotDao
import com.zktony.room.entities.Log
import com.zktony.room.entities.LogSnapshot
import com.zktony.room.entities.Program
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class LogRepository @Inject constructor(
    private val logDao: LogDao,
    private val logSnapshotDao: LogSnapshotDao
) {
    val scope = CoroutineScope(Dispatchers.IO)

    init {
        // 清理过期日志
        clearExpiredLog()
    }

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

    // 清理过期日志
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

    suspend fun deleteByIds(ids: List<Long>): Boolean {
        ids.forEach {
            logSnapshotDao.deleteBySubId(it)
        }
        val effect = logDao.deleteByIds(ids)
        return effect > 0
    }
}