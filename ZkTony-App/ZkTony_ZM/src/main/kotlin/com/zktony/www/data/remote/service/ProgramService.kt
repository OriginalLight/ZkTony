package com.zktony.www.data.remote.service

import com.zktony.www.data.remote.model.ProgramDTO
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ProgramService {
    @POST("program")
    fun uploadProgram(@Body programs: List<ProgramDTO>): Flow<Response<String>>
}