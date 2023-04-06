package com.zktony.manager.data.remote

import com.zktony.proto.*
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class OrderGrpc constructor(
    private val channel: ManagedChannel,
) {
    private val stub = OrderServiceGrpcKt.OrderServiceCoroutineStub(channel)

    suspend fun getOrderPage(page: OrderRequestPage) = flow {
        try {
            emit(stub.getOrderPage(page))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun searchOrder(search: OrderSearch) = flow {
        try {
            emit(stub.searchOrder(search))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getById(mid: String) = flow {
        try {
            emit(stub.getById(
                orderId {
                    id = mid
                }
            ))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun add(order: Order) = flow {
        try {
            emit(stub.addOrder(order))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun update(order: Order) = flow {
        try {
            emit(stub.updateOrder(order))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun delete(mid: String) = flow {
        try {
            emit(stub.deleteOrder(
                orderId {
                    id = mid
                }
            ))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)
}