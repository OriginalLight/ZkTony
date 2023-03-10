package com.zktony.manager.common.http

import com.zktony.manager.common.ext.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

/**
 * @author: 刘贺贺
 * @date: 2022-09-22 17:20
 */
object DownloadManager {
    fun download(url: String, file: File): Flow<DownloadState> {
        return flow {
            val request = Request.Builder().url(url).get().build()
            val response = OkHttpClient.Builder().build().newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.let { body ->
                    //文件大小
                    val totalLength = body.contentLength().toDouble()
                    //写文件
                    file.outputStream().run {
                        val input = body.byteStream()
                        input.copyTo(this, progress = {
                            val progress = (it / totalLength * 100).toInt()
                            emit(DownloadState.Progress(progress))
                        })
                    }

                    emit(DownloadState.Success(file))
                }
            } else {
                throw IOException(response.toString())
            }
        }.catch {
            file.delete()
            emit(DownloadState.Err(it))
        }.flowOn(Dispatchers.IO)
    }
}

sealed class DownloadState {
    data class Progress(val progress: Int) : DownloadState()
    data class Err(val t: Throwable) : DownloadState()
    data class Success(val file: File) : DownloadState()
}