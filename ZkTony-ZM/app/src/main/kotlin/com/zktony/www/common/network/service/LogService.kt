package com.zktony.www.common.network.service

import com.zktony.www.common.network.adapter.NetworkResponse
import com.zktony.www.common.network.model.LogDTO
import com.zktony.www.common.network.model.LogDetailDTO
import com.zktony.www.common.room.entity.LogData
import com.zktony.www.common.room.entity.LogRecord
import retrofit2.http.Body
import retrofit2.http.POST

interface LogService : BaseService {

    @POST("/log")
    suspend fun uploadLog(@Body logList: List<LogDTO>): NetworkResponse<Any>

    @POST("/log/detail")
    suspend fun uploadLogDetail(@Body logList: List<LogDetailDTO>): NetworkResponse<Any>
}