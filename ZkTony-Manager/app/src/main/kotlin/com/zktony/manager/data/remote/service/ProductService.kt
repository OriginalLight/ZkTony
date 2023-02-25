package com.zktony.manager.data.remote.service

import com.zktony.manager.data.remote.model.Equipment
import com.zktony.manager.data.remote.model.Product
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 13:24
 */
interface ProductService {

    @POST("/product")
    fun add(@Body product: Product): Flow<Response<String>>

    @PUT("/product")
    fun update(@Body product: Product): Flow<Response<String>>

    @DELETE("/product")
    fun delete(@Body id: String): Flow<Response<String>>

    @GET("/product")
    fun search(
        @Query("id") id: String,
        @Query("software_id") software_id: String,
        @Query("equipment_id") equipment_id: String,
        @Query("customer_id") customer_id: String,
        @Query("express_number") express_number: String,
        @Query("express_company") express_company: String,
        @Query("equipment_number") equipment_number: String,
        @Query("create_by") create_by: String,
        @Query("begin_time") begin_time: String,
        @Query("end_time") end_time: String
    ): Flow<Response<List<Equipment>>>
}