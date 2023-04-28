package com.zktony.protobuf.grpc

import com.zktony.proto.*
import com.zktony.protobuf.utils.Constants
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout

class LogGrpc constructor(
    private val channel: ManagedChannel,
) {
    private val stub = LogServiceGrpcKt.LogServiceCoroutineStub(channel)

    suspend fun addLogs(logs: List<Log>) = flow {
        try {
            withTimeout(Constants.TIME_OUT) {
                emit(stub.addLogs(logList { list.addAll(logs) }))
            }
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)
}