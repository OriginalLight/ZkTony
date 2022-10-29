package com.zktony.www.data.repository

import com.zktony.www.common.network.service.SystemService
import javax.inject.Inject

class SystemRepository @Inject constructor(
    private val systemService: SystemService
) {
    suspend fun getVersionInfo(id: Long) = systemService.getVersionInfo(id)
}