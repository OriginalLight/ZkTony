package com.zktony.www.services.impl

import com.zktony.www.common.http.RetrofitManager
import com.zktony.www.common.http.adapter.NetworkResponse
import com.zktony.www.data.entity.LogData
import com.zktony.www.data.entity.LogRecord
import com.zktony.www.services.LogService
import javax.inject.Inject

class LogServiceImpl @Inject constructor() : LogService {

    private val service by lazy { RetrofitManager.getService(LogService::class.java) }

    override suspend fun uploadLogRecords(logRecords: List<LogRecord>): NetworkResponse<Any> {
        return service.uploadLogRecords(logRecords)
    }

    override suspend fun uploadLogData(logDatas: List<LogData>): NetworkResponse<Any> {
        return service.uploadLogData(logDatas)
    }

}