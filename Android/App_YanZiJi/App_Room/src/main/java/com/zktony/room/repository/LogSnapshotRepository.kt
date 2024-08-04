package com.zktony.room.repository

import com.zktony.room.dao.LogSnapshotDao
import com.zktony.room.entities.LogSnapshot
import javax.inject.Inject

class LogSnapshotRepository @Inject constructor(
    private val logSnapshotDao: LogSnapshotDao
) {
    suspend fun insert(logSnapshot: LogSnapshot): Long {
        return logSnapshotDao.insert(logSnapshot)
    }

    suspend fun insertAll(logSnapshots: List<LogSnapshot>): List<Long> {
        return logSnapshotDao.insertAll(logSnapshots)
    }
}