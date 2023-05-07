package com.zktony.protobuf.grpc

import com.zktony.proto.*
import com.zktony.protobuf.utils.Constants
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout

class ProgramGrpc constructor(
    private val channel: ManagedChannel,
) {
    private val stub = ProgramServiceGrpcKt.ProgramServiceCoroutineStub(channel)

    suspend fun insertBatch(programs: List<Program>) = flow {
        try {
            withTimeout(Constants.TIME_OUT) {
                emit(stub.insertBatch(programList { list.addAll(programs) }))
            }
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)
}