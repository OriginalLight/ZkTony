package com.zktony.www.data.remote.service

import com.zktony.www.data.remote.adapter.NetworkResponse
import com.zktony.www.data.remote.model.ProgramDTO
import retrofit2.http.Body
import retrofit2.http.POST

interface ProgramService : BaseService {
    @POST("/program")
    suspend fun uploadProgram(@Body programs: List<ProgramDTO>): NetworkResponse<Any>
}