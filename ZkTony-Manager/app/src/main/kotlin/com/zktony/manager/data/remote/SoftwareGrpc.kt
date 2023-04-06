package com.zktony.manager.data.remote

import com.zktony.proto.*
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SoftwareGrpc constructor(
    private val channel: ManagedChannel,
) {
    private val stub = SoftwareServiceGrpcKt.SoftwareServiceCoroutineStub(channel)

    suspend fun getSoftwarePage(page: SoftwareRequestPage) = flow {
        try {
            emit(stub.getSoftwarePage(page))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun searchSoftware(search: SoftwareSearch) = flow {
        try {
            emit(stub.searchSoftware(search))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getById(mid: String) = flow {
        try {
            emit(stub.getById(
                softwareId {
                    id = mid
                }
            ))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun add(software: Software) = flow {
        try {
            emit(stub.addSoftware(software))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun update(software: Software) = flow {
        try {
            emit(stub.updateSoftware(software))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun delete(mid: String) = flow {
        try {
            emit(stub.deleteSoftware(
                softwareId {
                    id = mid
                }
            ))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)


}