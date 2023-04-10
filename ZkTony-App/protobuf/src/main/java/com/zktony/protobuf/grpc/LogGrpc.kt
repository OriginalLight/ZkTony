package com.zktony.protobuf.grpc

import com.zktony.proto.Log
import com.zktony.proto.LogServiceGrpcKt
import com.zktony.proto.logList
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class LogGrpc constructor(
    private val channel: ManagedChannel,
) {
    private val stub = LogServiceGrpcKt.LogServiceCoroutineStub(channel)

    suspend fun addLogs(logs: List<Log>) = flow {
        try {
            emit(stub.addLogs(logList { list.addAll(logs) }))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)
}