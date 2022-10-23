package com.zktony.www.data.services

import com.zktony.www.common.http.adapter.NetworkResponse
import com.zktony.www.data.entity.LogData
import com.zktony.www.data.entity.LogRecord
import retrofit2.http.Body
import retrofit2.http.POST

interface LogService : BaseService {

    @POST("/log/record")
    suspend fun uploadLogRecords(@Body logRecord: List<LogRecord>): NetworkResponse<Any>

    @POST("/log/data")
    suspend fun uploadLogData(@Body logData: List<LogData>): NetworkResponse<Any>
}