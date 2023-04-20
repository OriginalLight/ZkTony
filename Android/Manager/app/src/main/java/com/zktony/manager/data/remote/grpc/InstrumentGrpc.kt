package com.zktony.manager.data.remote.grpc

import com.zktony.proto.*
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class InstrumentGrpc constructor(
    private val channel: ManagedChannel,
) {
    private val stub = InstrumentServiceGrpcKt.InstrumentServiceCoroutineStub(channel)

    suspend fun getInstrumentPage(page: InstrumentRequestPage) = flow {
        try {
            emit(stub.getInstrumentPage(page))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun searchInstrument(search: InstrumentSearch) = flow {
        try {
            emit(stub.searchInstrument(search))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getById(mid: String) = flow {
        try {
            emit(stub.getById(
                instrumentId {
                    id = mid
                }
            ))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun add(instrument: Instrument) = flow {
        try {
            emit(stub.addInstrument(instrument))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun update(instrument: Instrument) = flow {
        try {
            emit(stub.updateInstrument(instrument))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun delete(mid: String) = flow {
        try {
            emit(stub.deleteInstrument(
                instrumentId {
                    id = mid
                }
            ))
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)


}