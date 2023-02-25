package com.zktony.manager.data.remote.service

import com.zktony.manager.data.remote.model.Customer
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 9:54
 */
interface CustomerService {

    @POST("/customer")
    fun add(@Body customer: Customer): Flow<Response<String>>

    fun update(@Body customer: Customer): Flow<Response<String>>

    @DELETE("/customer")
    fun delete(@Body id: String): Flow<Response<String>>

    @GET("/customer")
    fun search(
        @Query("id") id: String,
        @Query("name") name: String,
        @Query("phone") phone: String,
        @Query("address") address: String
    ): Flow<Response<List<Customer>>>
}