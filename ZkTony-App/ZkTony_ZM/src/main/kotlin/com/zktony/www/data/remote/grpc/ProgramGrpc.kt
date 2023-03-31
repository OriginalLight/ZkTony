package com.zktony.www.data.remote.grpc

import com.zktony.proto.Program
import com.zktony.proto.ProgramList
import com.zktony.proto.ProgramServiceGrpcKt
import io.grpc.ManagedChannel
import kotlinx.coroutines.flow.flow

class ProgramGrpc constructor(
    private val channel: ManagedChannel,
) {
    private val stub = ProgramServiceGrpcKt.ProgramServiceCoroutineStub(channel)

    suspend fun addPrograms(programs: List<Program>) = flow {
        try {
            val request = ProgramList.newBuilder()
                .addAllList(programs)
                .build()

            emit(stub.addPrograms(request))
        } catch (e: Exception) {
            throw e
        }
    }
}