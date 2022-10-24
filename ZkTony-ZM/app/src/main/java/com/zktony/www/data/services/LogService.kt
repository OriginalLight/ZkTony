package com.zktony.www.data.services

import com.zktony.www.common.http.adapter.NetworkResponse
import com.zktony.www.data.entity.LogData
import com.zktony.www.data.entity.LogRecord
import retrofit2.http.Body
import retrofit2.http.POST

interface LogService : BaseService {

    @POST("/log/record")
    suspend fun uploadLogRecords(@Body logRecordList: List<LogRecord>): NetworkResponse<Any>

    @POST("/log/data")
    suspend fun uploadLogData(@Body logDataList: List<LogData>): NetworkResponse<Any>
}