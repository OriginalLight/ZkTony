package com.zktony.www.data.repository

import com.zktony.www.common.network.adapter.toResult
import com.zktony.www.common.network.result.NetworkResult
import com.zktony.www.common.network.service.SystemService
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SystemRepository @Inject constructor(
    private val systemService: SystemService
) {
    suspend fun getVersionInfo(id: Long) = flow {
        emit(NetworkResult.Loading)
        emit(systemService.getVersionInfo(id).toResult())
    }
}