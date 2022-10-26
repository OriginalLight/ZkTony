package com.zktony.www.data.repository

import com.zktony.www.data.dao.LogDataDao
import com.zktony.www.data.entity.LogData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 8:56
 */
class LogDataRepository @Inject constructor(
    private val logDataDao: LogDataDao
) {

    suspend fun insert(logData: LogData) {
        logDataDao.insert(logData)
    }

    suspend fun deleteByDate() {
        logDataDao.deleteByDate()
    }

    suspend fun updateBatch(logDataList: List<LogData>) {
        logDataDao.updateBatch(logDataList)
    }

    fun getByLogId(id: String): Flow<List<LogData>> {
        return logDataDao.getByLogId(id)
    }

    suspend fun deleteDataLessThanTen() {
        logDataDao.deleteDataLessThanTen()
    }

    fun withoutUpload(): Flow<List<LogData>> {
        return logDataDao.withoutUpload()
    }
}