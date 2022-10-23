package com.zktony.www.ui.admin.model

import android.content.Context
import com.zktony.www.services.model.Version
import java.io.File

sealed class AdminIntent {
    data class ChangeInterval(val interval: Int) : AdminIntent()
    data class ChangeDuration(val duration: Int) : AdminIntent()
    data class ChangeBar(val bar: Boolean, val context: Context) : AdminIntent()
    data class ChangeAudio(val audio: Boolean) : AdminIntent()
    data class ChangeDetect(val detect: Boolean) : AdminIntent()
    data class ChangePump(val pump: Boolean) : AdminIntent()
    data class CheckUpdate(val context: Context) : AdminIntent()
    data class DoUpdate(val context: Context, val file: File?, val version: Version?) :
        AdminIntent()

    data class WifiSetting(val context: Context) : AdminIntent()
    object Rest : AdminIntent()
}
