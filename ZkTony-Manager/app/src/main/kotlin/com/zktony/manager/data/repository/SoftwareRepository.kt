package com.zktony.manager.data.repository

import com.zktony.manager.data.remote.model.Software
import com.zktony.manager.data.remote.model.SoftwareQueryDTO
import com.zktony.manager.data.remote.service.SoftwareService
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 13:39
 */
class SoftwareRepository @Inject constructor(
    private val service: SoftwareService
) {
    fun add(software: Software) = service.add(software)
    fun update(software: Software) = service.update(software)
    fun delete(id: String) = service.delete(id)
    fun search(dto: SoftwareQueryDTO) = service.search(
        dto.id,
        dto.`package`,
        dto.build_type
    )
}

