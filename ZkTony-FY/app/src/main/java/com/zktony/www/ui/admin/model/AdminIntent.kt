package com.zktony.www.ui.admin.model

import android.content.Context
import com.zktony.www.data.services.model.Version
import java.io.File

sealed class AdminIntent {
    // 下位机复位
    object Reset : AdminIntent()

    // 跳转到wifi设置界面
    data class WifiSetting(val context: Context) : AdminIntent()

    // 检查更新
    data class CheckUpdate(val context: Context) : AdminIntent()

    // 执行更新
    data class DoUpdate(val context: Context, val file: File?, val version: Version?) :
        AdminIntent()

    // 切换底部导航栏
    data class ChangeBar(val bar: Boolean, val context: Context) : AdminIntent()

    // 变更抗体保温温度
    data class ChangeTemp(val temp: Float) : AdminIntent()

}
