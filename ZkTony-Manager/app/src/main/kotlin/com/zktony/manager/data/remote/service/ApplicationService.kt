package com.zktony.manager.data.remote.service

import android.content.Context
import com.zktony.manager.data.remote.client.DownloadResult
import com.zktony.manager.data.remote.model.Application
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
class ApplicationService {
    suspend fun getApplicationById(id: String) = flow {
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

    suspend fun download(context: Context, url: String) = flow {
        val apk = context.filesDir.absolutePath + File.separator + "app.apk"
        emit(DownloadResult.Loading)
        RxHttp.get(url)
            .toDownloadFlow(
                destPath = apk,
                append = true,
                capacity = 1,
                progress = {
                    emit(DownloadResult.Progress(it.progress))
                }
            ).catch {
                emit(DownloadResult.Error(it))
            }.collect {
                emit(DownloadResult.Success(File(apk)))
            }
    }

}