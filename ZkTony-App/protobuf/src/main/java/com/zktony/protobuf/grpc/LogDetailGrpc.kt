package com.zktony.protobuf.grpc

import com.zktony.proto.LogDetail
import com.zktony.proto.LogDetailServiceGrpcKt
import com.zktony.proto.logDetailList
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
            emit(stub.addLogDetails(logDetailList { list.addAll(logs) }))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)
}