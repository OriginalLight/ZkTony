package com.zktony.manager.data.remote.provider

import com.zktony.manager.data.model.Application
import com.zktony.manager.data.remote.client.NetworkResult
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toFlowResponse

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 17:13
 */
object RemoteApplicationProvider {

    fun getApplicationById(id: String) = flow {
        emit(NetworkResult.Loading)
        RxHttp.get("/application")
            .add("application_id", id)
            .toFlowResponse<Application>()
            .catch {
                emit(NetworkResult.Error(it))
            }
            .collect {
                emit(NetworkResult.Success(it))
            }
    }

}