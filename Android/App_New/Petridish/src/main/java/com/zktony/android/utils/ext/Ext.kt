package com.zktony.android.utils.ext

import android.app.Application

/**
 * Created by 刘贺贺 (ง •̀_•́)ง
 */
object Ext {
    lateinit var ctx: Application

    fun with(app: Application) {
        ctx = app
    }
}