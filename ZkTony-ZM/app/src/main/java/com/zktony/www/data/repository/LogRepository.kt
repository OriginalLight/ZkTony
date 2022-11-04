package com.zktony.www.data.repository

import com.zktony.www.common.network.adapter.toResult
import com.zktony.www.common.room.entity.LogData
import com.zktony.www.common.room.entity.LogRecord
import com.zktony.www.common.network.service.LogService
import com.zktony.www.common.result.NetworkResult
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LogRepository @Inject constructor(
    private val service: LogService
) {
    suspend fun uploadLogRecords(logRecordList: List<LogRecord>) = flow {
        emit(NetworkResult.Loading)
        emit(service.uploadLogRecords(logRecordList).toResult())
    }

    suspend fun uploadLogData(logDataList: List<LogData>) = flow {
        emit(NetworkResult.Loading)
        emit(service.uploadLogData(logDataList).toResult())
    }
}