package com.zktony.www.data.remote.grpc

import com.zktony.proto.Log
import com.zktony.proto.LogList
import com.zktony.proto.LogServiceGrpcKt
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
            val request = LogList.newBuilder()
                .addAllList(logs)
                .build()

            emit(stub.addLogs(request))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)
}