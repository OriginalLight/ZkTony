package com.zktony.android.utils

import android.content.res.Resources

object ResourceUtils {
    // 上下文 LocalContext.current
    private lateinit var resources: Resources

    fun with(resources: Resources) {
        this.resources = resources
    }

    // 获取字符串资源
    fun stringResource(id: Int): String {
        return resources.getString(id)
    }

    // 获取字符串资源，并替换占位符
    fun stringResource(id: Int, vararg formatArgs: Any): String {
        return resources.getString(id, *formatArgs)
    }
}