package com.zktony.common.http.download

import java.io.File

interface DownloadListener {
    fun onProgress(bytesDownloaded: Int, bytesTotal: Int)
    fun onComplete(file: File)
    fun onError(e: Exception)
}