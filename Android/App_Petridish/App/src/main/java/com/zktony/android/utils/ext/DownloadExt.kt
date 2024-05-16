package com.zktony.android.utils.ext

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException


fun String.download(file: File): Flow<DownloadState> {
    val url = this
    return flow {
        val request = Request.Builder().url(url).get().build()
        val response = OkHttpClient.Builder().build().newCall(request).execute()
        if (response.isSuccessful) {
            response.body.let { body ->
                //文件大小
                val totalLength = body.contentLength().toDouble()
                //写文件
                file.outputStream().run {
                    val input = body.byteStream()
                    input.copyTo(this) { currentLength ->
                        //当前下载进度
                        val process = currentLength / totalLength * 100
                        emit(DownloadState.Progress(process.toInt()))
                    }
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

sealed class DownloadState {
    data class Progress(val progress: Int) : DownloadState()
    data class Err(val t: Throwable) : DownloadState()
    data class Success(val file: File) : DownloadState()
}