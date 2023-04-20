package com.zktony.core.dialog

import android.app.Application
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.style.MaterialStyle
import com.kongzue.dialogx.util.TextInfo
import com.zktony.core.BuildConfig

/**
 * @author: 刘贺贺
 * @date: 2022-10-08 9:38
 */
object DialogXManager {

    fun init(application: Application) {

        DialogX.init(application)
        //开启调试模式，在部分情况下会使用 Log 输出日志信息
        DialogX.DEBUGMODE = BuildConfig.DEBUG

        //设置主题样式
        DialogX.globalStyle = MaterialStyle.style()

        //设置亮色/暗色（在启动下一个对话框时生效）
        DialogX.globalTheme = DialogX.THEME.LIGHT

        //设置对话框最大宽度（单位为像素）
        DialogX.dialogMaxWidth = 450

        //设置 InputDialog 自动弹出键盘
        DialogX.autoShowInputKeyboard = true
    }
}