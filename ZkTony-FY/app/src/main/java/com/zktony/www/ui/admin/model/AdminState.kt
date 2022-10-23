package com.zktony.www.ui.admin.model

import com.zktony.www.data.services.model.Version
import java.io.File

sealed class AdminState {
    data class CheckUpdate(val file: File?, val version: Version?) : AdminState()
    data class DownloadProgress(val progress: Int) : AdminState()
    data class DownloadSuccess(val file: File) : AdminState()
    object DownloadError : AdminState()
    object ChangeBar : AdminState()
}
