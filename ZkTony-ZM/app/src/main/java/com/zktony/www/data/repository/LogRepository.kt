package com.zktony.www.data.repository

import com.zktony.www.common.http.adapter.NetworkResponse
import com.zktony.www.data.entity.LogData
import com.zktony.www.data.entity.LogRecord
import com.zktony.www.data.services.LogService
import javax.inject.Inject

class LogRepository @Inject constructor(
    private val service: LogService
) {
    suspend fun uploadLogRecords(logRecordList: List<LogRecord>): NetworkResponse<Any> {
        return service.uploadLogRecords(logRecordList)
    }

    suspend fun uploadLogData(logDataList: List<LogData>): NetworkResponse<Any> {
        return service.uploadLogData(logDataList)
    }
}