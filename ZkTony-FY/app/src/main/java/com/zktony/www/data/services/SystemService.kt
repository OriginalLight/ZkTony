package com.zktony.www.data.services

import com.zktony.www.common.http.adapter.NetworkResponse
import com.zktony.www.data.services.model.Version
import retrofit2.http.GET
import retrofit2.http.Query

interface SystemService : BaseService {

    @GET("/version")
    suspend fun getVersionInfo(@Query("id") id: Long): NetworkResponse<Version>
}