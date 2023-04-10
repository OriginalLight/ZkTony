package com.zktony.protobuf.grpc

import com.zktony.proto.ApplicationServiceGrpcKt
import com.zktony.proto.applicationSearch
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ApplicationGrpc constructor(
    private val channel: ManagedChannel,
) {
    private val stub = ApplicationServiceGrpcKt.ApplicationServiceCoroutineStub(channel)


    suspend fun getByApplicationId(id: String) = flow {
        try {
            emit(stub.getByApplicationId(applicationSearch { applicationId = id }))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)
}