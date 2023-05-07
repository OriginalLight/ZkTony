package com.zktony.protobuf.grpc

import com.zktony.proto.*
import com.zktony.protobuf.utils.Constants
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout

class ApplicationGrpc constructor(
    private val channel: ManagedChannel,
) {
    private val stub = ApplicationServiceGrpcKt.ApplicationServiceCoroutineStub(channel)


    suspend fun query(query: ApplicationRequestQuery) = flow {
        try {
            withTimeout(Constants.TIME_OUT) {
                emit(stub.query(query))
            }
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getApplication(app: String) = flow {
        try {
            withTimeout(Constants.TIME_OUT) {
                val query = stub.query(
                    applicationRequestQuery {
                        page = 1
                        pageSize = 1
                        id = 0
                        applicationId = app
                        buildType = ""

                    }
                )
                if(query.listList.isNotEmpty()) {
                    emit(query.getList(0))
                } else {
                    throw Exception("Application not found")
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)
}