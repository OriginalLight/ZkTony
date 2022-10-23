package com.zktony.www.ui.admin.model

import com.zktony.www.services.model.Version
import java.io.File

sealed class AdminState {
    data class ChangeInterval(val interval: Int) : AdminState()
    data class ChangeDuration(val duration: Int) : AdminState()
    data class ChangeBar(val bar: Boolean) : AdminState()
    data class ChangeAudio(val audio: Boolean) : AdminState()
    data class ChangeDetect(val detect: Boolean) : AdminState()
    data class CheckUpdate(val file: File?, val version: Version?) : AdminState()
    data class DownloadProgress(val progress: Int) : AdminState()
    data class DownloadSuccess(val file: File) : AdminState()
    object DownloadError : AdminState()
}
