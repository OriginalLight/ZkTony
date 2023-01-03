package com.zktony.www.data.repository

import com.zktony.www.data.dao.LogDataDao
import com.zktony.www.data.model.LogData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 8:56
 */
class LogDataRepository @Inject constructor(
    private val dao: LogDataDao
) {

    suspend fun insert(logData: LogData) {
        dao.insert(logData)
    }

    suspend fun deleteByDate() {
        dao.deleteByDate()
    }

    suspend fun updateBatch(logDatas: List<LogData>) {
        dao.updateBatch(logDatas)
    }

    fun getByLogId(id: String): Flow<List<LogData>> {
        return dao.getByLogId(id)
    }

    suspend fun deleteDataLessThanTen() {
        dao.deleteDataLessThanTen()
    }

    fun withoutUpload(): Flow<List<LogData>> {
        return dao.withoutUpload()
    }
}