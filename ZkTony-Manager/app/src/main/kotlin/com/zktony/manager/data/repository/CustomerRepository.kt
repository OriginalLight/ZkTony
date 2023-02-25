package com.zktony.manager.data.repository

import com.zktony.manager.data.remote.model.Customer
import com.zktony.manager.data.remote.model.CustomerQueryDTO
import com.zktony.manager.data.remote.service.CustomerService
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 13:39
 */
class CustomerRepository @Inject constructor(
    private val service: CustomerService
) {
    fun save(customer: Customer) = service.save(customer)
    fun delete(id: String) = service.delete(id)
    fun search(dto: CustomerQueryDTO) = service.search(
        dto.id,
        dto.name,
        dto.phone,
        dto.address
    )
}

