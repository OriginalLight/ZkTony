package com.zktony.www.common.network.service

import com.zktony.www.common.network.adapter.NetworkResponse
import com.zktony.www.common.room.entity.Log
import retrofit2.http.Body
import retrofit2.http.POST

interface LogService : BaseService {

    @POST("/log/record")
    suspend fun uploadLogRecords(@Body log: List<Log>): NetworkResponse<Any>
}