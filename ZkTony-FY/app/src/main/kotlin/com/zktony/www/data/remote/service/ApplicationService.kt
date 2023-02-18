package com.zktony.www.data.remote.service

import com.zktony.www.data.remote.adapter.NetworkResponse
import com.zktony.www.data.remote.model.Application
import retrofit2.http.GET
import retrofit2.http.Query

interface ApplicationService : BaseService {

    @GET("/application")
    suspend fun getById(@Query("application_id") id: String): NetworkResponse<Application>
}