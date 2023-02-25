package com.zktony.manager.data.repository

import com.zktony.manager.data.remote.model.Equipment
import com.zktony.manager.data.remote.model.EquipmentQueryDTO
import com.zktony.manager.data.remote.service.EquipmentService
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 13:39
 */
class EquipmentRepository @Inject constructor(
    private val service: EquipmentService
) {
    fun add(equipment: Equipment) = service.add(equipment)
    fun update(equipment: Equipment) = service.update(equipment)
    fun delete(id: String) = service.delete(id)
    fun search(dto: EquipmentQueryDTO) = service.search(
        dto.id,
        dto.name,
        dto.model,
        dto.begin_time,
        dto.end_time
    )
}

