package com.zktony.www.services.impl

import com.zktony.www.common.http.RetrofitManager
import com.zktony.www.common.http.adapter.NetworkResponse
import com.zktony.www.data.entity.Program
import com.zktony.www.services.ProgramService
import javax.inject.Inject

class ProgramServiceImpl @Inject constructor() : ProgramService {

    private val service by lazy { RetrofitManager.getService(ProgramService::class.java) }

    override suspend fun uploadProgram(programs: List<Program>): NetworkResponse<Any> {
        return service.uploadProgram(programs)
    }

}