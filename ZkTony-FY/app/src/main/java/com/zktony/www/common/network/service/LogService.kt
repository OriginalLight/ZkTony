package com.zktony.www.common.network.service

import com.zktony.www.common.network.adapter.NetworkResponse
import com.zktony.www.common.room.entity.LogData
import com.zktony.www.common.room.entity.LogRecord
import retrofit2.http.Body
import retrofit2.http.POST

interface LogService : BaseService {

    @POST("/log/record")
    suspend fun uploadLogRecords(@Body logRecord: List<LogRecord>): NetworkResponse<Any>

    @POST("/log/data")
    suspend fun uploadLogData(@Body logData: List<LogData>): NetworkResponse<Any>
}