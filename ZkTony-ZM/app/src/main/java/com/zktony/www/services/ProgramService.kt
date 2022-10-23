package com.zktony.www.services

import com.zktony.www.common.http.adapter.NetworkResponse
import com.zktony.www.data.entity.Program
import retrofit2.http.Body
import retrofit2.http.POST

interface ProgramService : BaseService {
    @POST("/program")
    suspend fun uploadProgram(@Body programs: List<Program>): NetworkResponse<Any>
}