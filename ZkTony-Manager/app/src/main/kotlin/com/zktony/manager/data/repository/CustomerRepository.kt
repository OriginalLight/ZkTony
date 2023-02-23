package com.zktony.manager.data.repository

import com.zktony.manager.data.remote.model.Customer
import com.zktony.manager.data.remote.model.CustomerQueryDTO
import com.zktony.manager.data.remote.result.getNetworkResult
import com.zktony.manager.data.remote.service.CustomerService
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 13:39
 */
class CustomerRepository @Inject constructor(
    private val service: CustomerService
) {
    fun add(customer: Customer) = service.add(customer).getNetworkResult()
    fun update(customer: Customer) = service.update(customer).getNetworkResult()
    fun delete(id: String) = service.delete(id).getNetworkResult()
    fun search(dto: CustomerQueryDTO) = service.search(
        dto.id,
        dto.name,
        dto.phone,
        dto.address
    ).getNetworkResult()
}

