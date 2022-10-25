package com.zktony.www.data.repository

import com.zktony.www.data.services.SystemService
import javax.inject.Inject

class SystemRepository @Inject constructor(
    private val systemService: SystemService
) {
    suspend fun getVersionInfo(id: Long) = systemService.getVersionInfo(id)
}