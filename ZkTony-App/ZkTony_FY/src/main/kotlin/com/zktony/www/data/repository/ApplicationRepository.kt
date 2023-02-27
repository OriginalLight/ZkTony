package com.zktony.www.data.repository

import com.zktony.www.BuildConfig
import com.zktony.www.data.remote.service.ApplicationService
import javax.inject.Inject

class ApplicationRepository @Inject constructor(
    private val service: ApplicationService
) {
    fun getById(id: String = BuildConfig.APPLICATION_ID) = service.getById(id)
}