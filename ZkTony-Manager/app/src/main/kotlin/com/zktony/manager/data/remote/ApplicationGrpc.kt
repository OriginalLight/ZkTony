package com.zktony.manager.data.remote

import com.zktony.manager.BuildConfig
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

    suspend fun getByApplicationId(
    ) = flow {
        try {
            emit(stub.getByApplicationId(applicationSearch {
                applicationId = BuildConfig.APPLICATION_ID

            }))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

}