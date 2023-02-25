package com.zktony.manager.data.repository

import com.zktony.manager.data.remote.model.Product
import com.zktony.manager.data.remote.model.ProductQueryDTO
import com.zktony.manager.data.remote.service.ProductService
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 13:39
 */
class ProductRepository @Inject constructor(
    private val service: ProductService
) {

    fun add(product: Product) = service.add(product)
    fun update(product: Product) = service.update(product)
    fun delete(id: String) = service.delete(id)
    fun search(dto: ProductQueryDTO) = service.search(
        dto.id,
        dto.software_id,
        dto.equipment_id,
        dto.customer_id,
        dto.express_number,
        dto.express_company,
        dto.equipment_number,
        dto.create_by,
        dto.begin_time,
        dto.end_time
    )
}

