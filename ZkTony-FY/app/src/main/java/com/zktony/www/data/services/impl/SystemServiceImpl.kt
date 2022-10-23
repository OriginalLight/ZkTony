package com.zktony.www.data.services.impl

import com.zktony.www.common.http.RetrofitManager
import com.zktony.www.common.http.adapter.NetworkResponse
import com.zktony.www.data.services.SystemService
import com.zktony.www.data.services.model.Version
import javax.inject.Inject

class SystemServiceImpl @Inject constructor() : SystemService {

    private val service by lazy { RetrofitManager.getService(SystemService::class.java) }

    override suspend fun getVersionInfo(id: Long): NetworkResponse<Version> {
        return service.getVersionInfo(id)
    }
}