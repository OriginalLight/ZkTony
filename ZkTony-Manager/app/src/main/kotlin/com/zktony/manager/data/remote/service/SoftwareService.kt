package com.zktony.manager.data.remote.service

import com.google.gson.Gson
import com.zktony.manager.data.remote.client.NetworkResult
import com.zktony.manager.data.remote.model.Software
import com.zktony.manager.data.remote.model.SoftwareQueryDTO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import rxhttp.toFlow
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toFlowResponse

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 13:24
 */
class SoftwareService {

    suspend fun add(softWare: Software) = flow {
        emit(NetworkResult.Loading)
        val json = Gson().toJson(softWare)
        RxHttp.postJson("/software")
            .addAll(json)
            .toFlow<String>()
            .catch {
                emit(NetworkResult.Error(it))
            }
            .collect {
                emit(NetworkResult.Success(it))
            }
    }

    suspend fun update(softWare: Software) = flow {
        emit(NetworkResult.Loading)
        val json = Gson().toJson(softWare)
        RxHttp.putJson("/software")
            .addAll(json)
            .toFlow<String>()
            .catch {
                emit(NetworkResult.Error(it))
            }
            .collect {
                emit(NetworkResult.Success(it))
            }
    }

    suspend fun delete(id: String) = flow {
        emit(NetworkResult.Loading)
        val json = Gson().toJson(id)
        RxHttp.deleteJson("/software")
            .addAll(json)
            .toFlow<String>()
            .catch {
                emit(NetworkResult.Error(it))
            }
            .collect {
                emit(NetworkResult.Success(it))
            }
    }

    suspend fun get(dto: SoftwareQueryDTO) = flow {
        emit(NetworkResult.Loading)
        RxHttp.get("/software")
            .add("id", dto.id)
            .add("package", dto.`package`)
            .add("build_type", dto.build_type)
            .toFlowResponse<List<Software>>()
            .catch {
                emit(NetworkResult.Error(it))
            }
            .collect {
                emit(NetworkResult.Success(it))
            }
    }
}