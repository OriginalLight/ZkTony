package com.zktony.android.utils

import android.annotation.SuppressLint
import android.app.Application
import com.hzmct.enjoysdk.api.EnjoySDK

object HzmctUtils {
    private lateinit var ctx: Application
    @SuppressLint("StaticFieldLeak")
    private lateinit var enjoySdk : EnjoySDK

    fun with(app: Application) {
        this.ctx = app
        enjoySdk = EnjoySDK(app)
        enjoySdk.setSecurePasswd("Abc123456", "Abc123456")
        enjoySdk.registSafeProgram("Abc123456")
    }

    fun setNavigationBar(status: Boolean) {
        enjoySdk.setNavigationBarShowStatus(if (status) 1 else 0)
    }

    fun setStatusBar(status: Boolean) {
        enjoySdk.setStatusBarShowStatus(status)
    }

    fun setHomePackage(packageName: String) {
        enjoySdk.setHomePackage(packageName)
    }
}