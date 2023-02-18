package com.zktony.www.data.repository

import com.zktony.www.data.remote.adapter.toResult
import com.zktony.www.data.remote.model.LogDTO
import com.zktony.www.data.remote.model.LogDetailDTO
import com.zktony.www.data.remote.result.NetworkResult
import com.zktony.www.data.remote.service.LogService
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LogRepository @Inject constructor(
    private val service: LogService
) {
    suspend fun uploadLogRecords(logRecordList: List<LogDTO>) = flow {
        emit(NetworkResult.Loading)
        emit(service.uploadLog(logRecordList).toResult())
    }

    suspend fun uploadLogData(logDataList: List<LogDetailDTO>) = flow {
        emit(NetworkResult.Loading)
        emit(service.uploadLogDetail(logDataList).toResult())
    }
}