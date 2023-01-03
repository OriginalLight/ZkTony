package com.zktony.www.data.service

import com.zktony.www.common.network.adapter.NetworkResponse
import com.zktony.www.data.model.LogData
import com.zktony.www.data.model.LogRecord
import retrofit2.http.Body
import retrofit2.http.POST

interface LogService : BaseService {

    @POST("/log/record")
    suspend fun uploadLogRecords(@Body logRecordList: List<LogRecord>): NetworkResponse<Any>

    @POST("/log/data")
    suspend fun uploadLogData(@Body logDataList: List<LogData>): NetworkResponse<Any>
}