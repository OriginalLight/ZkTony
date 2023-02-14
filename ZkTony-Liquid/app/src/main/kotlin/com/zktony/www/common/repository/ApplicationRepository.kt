package com.zktony.www.common.repository

import com.zktony.www.BuildConfig
import com.zktony.www.common.network.adapter.toResult
import com.zktony.www.common.network.result.NetworkResult
import com.zktony.www.common.network.service.ApplicationService
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ApplicationRepository @Inject constructor(
    private val service: ApplicationService
) {
    suspend fun getById(id: String = BuildConfig.APPLICATION_ID) = flow {
        emit(NetworkResult.Loading)
        emit(service.getById(id).toResult())
    }
}