package com.zktony.protobuf.grpc

import com.zktony.proto.ApplicationServiceGrpcKt
import com.zktony.proto.applicationSearch
import com.zktony.protobuf.utils.Constants
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout

class ApplicationGrpc constructor(
    private val channel: ManagedChannel,
) {
    private val stub = ApplicationServiceGrpcKt.ApplicationServiceCoroutineStub(channel)


    suspend fun getByApplicationId(id: String) = flow {
        try {
            withTimeout(Constants.TIME_OUT) {
                emit(stub.getByApplicationId(applicationSearch { applicationId = id }))
            }
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)
}