package com.zktony.www.data.service

import com.zktony.www.common.network.adapter.NetworkResponse
import com.zktony.www.data.model.Program
import retrofit2.http.Body
import retrofit2.http.POST

interface ProgramService : BaseService {
    @POST("/program")
    suspend fun uploadProgram(@Body programs: List<Program>): NetworkResponse<Any>
}