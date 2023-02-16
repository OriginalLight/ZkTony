package com.zktony.manager.data.store

import com.zktony.manager.data.remote.model.Software
import com.zktony.manager.data.remote.model.SoftwareQueryDTO
import com.zktony.manager.data.remote.service.SoftwareService

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 13:39
 */
class SoftwareStore(
    private val service: SoftwareService
) {
    suspend fun add(softWare: Software) = service.add(softWare)
    suspend fun update(softWare: Software) = service.update(softWare)
    suspend fun delete(id: String) = service.delete(id)
    suspend fun get(dto: SoftwareQueryDTO) = service.get(dto)

}