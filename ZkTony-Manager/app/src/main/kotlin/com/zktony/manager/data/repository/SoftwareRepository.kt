package com.zktony.manager.data.repository

import com.zktony.manager.data.remote.model.Software
import com.zktony.manager.data.remote.model.SoftwareQueryDTO
import com.zktony.manager.data.remote.result.getNetworkResult
import com.zktony.manager.data.remote.service.SoftwareService
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 13:39
 */
class SoftwareRepository @Inject constructor(
    private val service: SoftwareService
) {
    fun add(software: Software) = service.add(software).getNetworkResult()
    fun update(software: Software) = service.update(software).getNetworkResult()
    fun delete(id: String) = service.delete(id).getNetworkResult()
    fun get(dto: SoftwareQueryDTO) = service.get(dto).getNetworkResult()

}

