package com.zktony.manager.data.remote.service

import com.zktony.manager.data.remote.model.Equipment
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 13:24
 */
interface EquipmentService {

    @POST("/equipment")
    fun save(@Body equipment: Equipment): Flow<Response<String>>

    @DELETE("/equipment")
    fun delete(@Body id: String): Flow<Response<String>>

    @GET("/equipment")
    fun search(
        @Query("id") id: String,
        @Query("name") name: String,
        @Query("model") model: String,
        @Query("begin_time") begin_time: String,
        @Query("end_time") end_time: String
    ): Flow<Response<List<Equipment>>>
}