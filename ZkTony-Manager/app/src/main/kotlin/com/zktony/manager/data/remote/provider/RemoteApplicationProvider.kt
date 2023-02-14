package com.zktony.manager.data.remote.provider

import android.content.Context
import android.os.Environment
import com.zktony.manager.data.model.Application
import com.zktony.manager.data.remote.client.NetworkResult
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import rxhttp.toDownloadFlow
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toFlowResponse
import java.io.File

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

    fun downloadApplication(url: String) = flow {
        emit(NetworkResult.Loading)
        val destPath = Environment.getDownloadCacheDirectory().absolutePath + File.separator + "zm.apk"
        RxHttp.get(url)
            .toDownloadFlow(
                destPath = destPath,
                append = true,
                capacity = 1,
                progress = {
                    emit(NetworkResult.Progress(it.progress))
                }
            ).catch {
                emit(NetworkResult.Error(it))
            }.collect {
                emit(NetworkResult.Success(File(destPath)))
            }
    }



}