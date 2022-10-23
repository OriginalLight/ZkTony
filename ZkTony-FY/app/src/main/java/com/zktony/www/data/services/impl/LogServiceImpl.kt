package com.zktony.www.data.services.impl

import com.zktony.www.common.http.RetrofitManager
import com.zktony.www.common.http.adapter.NetworkResponse
import com.zktony.www.data.entity.LogData
import com.zktony.www.data.entity.LogRecord
import com.zktony.www.data.services.LogService
import javax.inject.Inject

class LogServiceImpl @Inject constructor() : LogService {

    private val service by lazy { RetrofitManager.getService(LogService::class.java) }

    override suspend fun uploadLogRecords(logRecord: List<LogRecord>): NetworkResponse<Any> {
        return service.uploadLogRecords(logRecord)
    }

    override suspend fun uploadLogData(logData: List<LogData>): NetworkResponse<Any> {
        return service.uploadLogData(logData)
    }

}