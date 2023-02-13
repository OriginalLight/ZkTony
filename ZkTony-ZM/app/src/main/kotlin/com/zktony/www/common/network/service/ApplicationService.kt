package com.zktony.www.common.network.service

import com.zktony.www.common.network.adapter.NetworkResponse
import com.zktony.www.common.network.model.Application
import retrofit2.http.GET
import retrofit2.http.Query

interface ApplicationService : BaseService {

    @GET("/application")
    suspend fun getById(@Query("application_id") id: String): NetworkResponse<Application>
}