package com.zktony.www.data.remote.service

import com.zktony.www.data.remote.model.Application
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApplicationService {

    @GET("/application")
    fun getById(@Query("application_id") id: String): Flow<Response<Application>>
}