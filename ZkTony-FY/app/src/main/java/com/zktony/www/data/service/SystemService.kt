package com.zktony.www.data.service

import com.zktony.www.common.network.adapter.NetworkResponse
import com.zktony.www.data.model.Version
import retrofit2.http.GET
import retrofit2.http.Query

interface SystemService : BaseService {

    @GET("/version")
    suspend fun getVersionInfo(@Query("id") id: Long): NetworkResponse<Version>
}