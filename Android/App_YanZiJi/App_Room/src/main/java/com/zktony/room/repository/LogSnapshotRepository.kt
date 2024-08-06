package com.zktony.room.repository

import com.zktony.room.dao.LogSnapshotDao
import com.zktony.room.entities.LogSnapshot
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LogSnapshotRepository @Inject constructor(
    private val logSnapshotDao: LogSnapshotDao
) {
    /**
     * Insert.
     * @param logSnapshot LogSnapshot.
     * @return Long.
     */
    suspend fun insert(logSnapshot: LogSnapshot): Long {
        return logSnapshotDao.insert(logSnapshot)
    }

    /**
     * Insert all.
     * @param logSnapshots List<LogSnapshot>.
     * @return List<Long>.
     */
    suspend fun insertAll(logSnapshots: List<LogSnapshot>): List<Long> {
        return logSnapshotDao.insertAll(logSnapshots)
    }

    /**
     * Get by subId.
     * @param subId Long.
     * @return Flow<List<LogSnapshot>>.
     */
    fun getBySubId(subId: Long): Flow<List<LogSnapshot>> {
        return logSnapshotDao.getBySubId(subId)
    }
}