package com.zktony.manager.data.remote

import com.zktony.proto.*
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CustomerGrpc constructor(
    private val channel: ManagedChannel,
) {
    private val stub = CustomerServiceGrpcKt.CustomerServiceCoroutineStub(channel)

    suspend fun getCustomerPage(page: CustomerRequestPage) = flow {
        try {
            emit(stub.getCustomerPage(page))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun searchCustomer(search: CustomerSearch) = flow {
        try {
            emit(stub.searchCustomer(search))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getById(mid: String) = flow {
        try {
            emit(stub.getById(customerId {
                id = mid
            }))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun add(customer: Customer) = flow {
        try {
            emit(stub.addCustomer(customer))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun update(customer: Customer) = flow {
        try {
            emit(stub.updateCustomer(customer))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun delete(mid: String) = flow {
        try {
            emit(stub.deleteCustomer(customerId {
                id = mid
            }))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

}