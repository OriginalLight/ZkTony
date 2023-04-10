package com.zktony.protobuf.grpc

import com.zktony.proto.Program
import com.zktony.proto.ProgramServiceGrpcKt
import com.zktony.proto.programList
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ProgramGrpc constructor(
    private val channel: ManagedChannel,
) {
    private val stub = ProgramServiceGrpcKt.ProgramServiceCoroutineStub(channel)

    suspend fun addPrograms(programs: List<Program>) = flow {
        try {
            emit(stub.addPrograms(programList { list.addAll(programs) }))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)
}