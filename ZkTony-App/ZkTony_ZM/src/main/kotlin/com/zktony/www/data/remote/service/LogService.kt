package com.zktony.www.data.remote.service

import com.zktony.www.data.remote.model.LogDTO
import com.zktony.www.data.remote.model.LogDetailDTO
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LogService {

    @POST("/log")
    fun uploadLog(@Body logList: List<LogDTO>): Flow<Response<String>>

    @POST("/log/detail")
    fun uploadLogDetail(@Body logList: List<LogDetailDTO>): Flow<Response<String>>
}