package com.zktony.android.utils

import android.annotation.SuppressLint
import android.app.Application
import com.hzmct.enjoysdk.api.EnjoySDK

object HzmctUtils {
    @SuppressLint("StaticFieldLeak")
    private lateinit var enjoySdk : EnjoySDK

    fun with() {
        enjoySdk = EnjoySDK(ApplicationUtils.ctx)
        try {
            enjoySdk.setSecurePasswd("Abc123456", "Abc123456")
            enjoySdk.registSafeProgram("Abc123456")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setNavigationBar(status: Boolean) : Boolean {
        try {
            val res = enjoySdk.setNavigationBarShowStatus(if (status) 1 else 0)
            return res != -1
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun setStatusBar(status: Boolean): Boolean {
        try {
            val res = enjoySdk.setStatusBarShowStatus(status)
            return res != -1
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun getHomePackage(): String? {
        try {
            return enjoySdk.homePackage
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun setHomePackage(packageName: String) : Boolean {
        try {
            val res = enjoySdk.setHomePackage(packageName)
            return res != -1
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}