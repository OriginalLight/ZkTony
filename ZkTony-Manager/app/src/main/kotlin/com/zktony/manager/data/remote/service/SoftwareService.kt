package com.zktony.manager.data.remote.service

import com.zktony.manager.data.remote.model.Software
import com.zktony.manager.data.remote.model.SoftwareQueryDTO
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 13:24
 */
interface SoftwareService {

    @POST("/software")
    fun add(@Body softWare: Software) : Flow<Response<String>>

    @PUT("/software")
    fun update(@Body softWare: Software) : Flow<Response<String>>

    @DELETE("/software")
    fun delete(@Body id: String) : Flow<Response<String>>

    @GET("/software")
    fun get(@Body dto: SoftwareQueryDTO): Flow<Response<List<Software>>>
}