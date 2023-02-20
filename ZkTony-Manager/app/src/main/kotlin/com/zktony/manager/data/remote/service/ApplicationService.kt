package com.zktony.manager.data.remote.service

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 17:13
 */
import com.zktony.manager.data.remote.model.Application
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApplicationService {

    @GET("/application")
    fun getById(@Query("application_id") id: String): Flow<Response<Application>>
}