package com.zktony.www.data.remote.grpc

import com.zktony.proto.LogDetail
import com.zktony.proto.LogDetailList
import com.zktony.proto.LogDetailServiceGrpcKt
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class LogDetailGrpc constructor(
    private val channel: ManagedChannel,
) {
    private val stub = LogDetailServiceGrpcKt.LogDetailServiceCoroutineStub(channel)

    suspend fun addLogDetails(logs: List<LogDetail>) = flow {
        try {
            val request = LogDetailList.newBuilder()
                .addAllList(logs)
                .build()

            emit(stub.addLogDetails(request))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)
}