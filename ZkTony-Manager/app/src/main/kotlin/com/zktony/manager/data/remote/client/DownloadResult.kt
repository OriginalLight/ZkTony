package com.zktony.manager.data.remote.client

import java.io.File

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 16:16
 */
sealed class DownloadResult {
    object Loading : DownloadResult()
    data class Progress(val progress: Int) : DownloadResult()
    data class Success(val file: File) : DownloadResult()
    data class Error(val throwable: Throwable) : DownloadResult()
}
