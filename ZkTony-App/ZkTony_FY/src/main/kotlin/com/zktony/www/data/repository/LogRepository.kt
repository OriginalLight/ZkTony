package com.zktony.www.data.repository

import com.zktony.www.data.local.room.dao.LogDao
import com.zktony.www.data.local.room.entity.Log
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 8:52
 */
class LogRepository @Inject constructor(
    private val dao: LogDao
) {
    suspend fun insert(log: Log) {
        dao.insert(log)
    }

    suspend fun update(log: Log) {
        dao.update(log)
    }

    suspend fun delete(log: Log) {
        dao.delete(log)
    }

    suspend fun deleteByDate() {
        dao.deleteByDate()
    }

    fun getAll(): Flow<List<Log>> {
        return dao.getAll()
    }

    fun getByDate(start: Date, end: Date): Flow<List<Log>> {
        return dao.getByDate(start, end)
    }
}