package com.zktony.www.common.http.download.model

import java.io.File

/**
 * @author: 刘贺贺
 * @date: 2022-09-22 17:19
 */
sealed class DownloadState {
    data class Progress(val progress: Int) : DownloadState()
    data class Err(val t: Throwable) : DownloadState()
    data class Success(val file: File) : DownloadState()
}