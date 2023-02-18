package com.zktony.www.data.remote.service

import com.zktony.www.data.remote.adapter.NetworkResponse
import com.zktony.www.data.remote.model.LogDTO
import com.zktony.www.data.remote.model.LogDetailDTO
import retrofit2.http.Body
import retrofit2.http.POST

interface LogService : BaseService {

    @POST("/log")
    suspend fun uploadLog(@Body logList: List<LogDTO>): NetworkResponse<Any>

    @POST("/log/detail")
    suspend fun uploadLogDetail(@Body logList: List<LogDetailDTO>): NetworkResponse<Any>
}