package com.zktony.www.common.repository

import com.zktony.www.common.network.adapter.toResult
import com.zktony.www.common.network.model.LogDTO
import com.zktony.www.common.network.model.LogDetailDTO
import com.zktony.www.common.network.service.LogService
import com.zktony.www.common.network.result.NetworkResult
import com.zktony.www.common.room.entity.LogData
import com.zktony.www.common.room.entity.LogRecord
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