package com.zktony.www.data.remote.grpc

import com.zktony.proto.ApplicationSearch
import com.zktony.proto.ApplicationServiceGrpcKt
import com.zktony.www.BuildConfig
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ApplicationGrpc constructor(
    private val channel: ManagedChannel,
) {
    private val stub = ApplicationServiceGrpcKt.ApplicationServiceCoroutineStub(channel)


    suspend fun getByApplicationId(
        applicationId: String = BuildConfig.APPLICATION_ID
    ) = flow {
        try {
            val request = ApplicationSearch.newBuilder()
                .setApplicationId(applicationId)
                .build()

            val response = stub.getByApplicationId(request)
            emit(response)
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)
}