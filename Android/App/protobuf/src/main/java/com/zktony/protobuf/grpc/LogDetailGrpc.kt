package com.zktony.protobuf.grpc

import com.zktony.proto.*
import com.zktony.protobuf.utils.Constants
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout

class LogDetailGrpc constructor(
    private val channel: ManagedChannel,
) {
    private val stub = LogDetailServiceGrpcKt.LogDetailServiceCoroutineStub(channel)

    suspend fun insertBatch(logs: List<LogDetail>) = flow {
        try {
            withTimeout(Constants.TIME_OUT) {
                emit(stub.insertBatch(logDetailList { list.addAll(logs) }))
            }
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)
}