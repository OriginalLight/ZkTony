package com.zktony.android.utils

import android.app.Application
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.LocaleList
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import com.zktony.log.LogUtils
import java.io.File
import java.util.Locale

/**
 * @author 刘贺贺
 * @date 2023/9/13 8:56
 */
object ApplicationUtils {
    lateinit var ctx: Application

    fun with(app: Application) {
        ctx = app
        withCrashHandler()
    }

    // Set up a global exception catcher
    private fun withCrashHandler() {
        Thread.currentThread().setUncaughtExceptionHandler { _, exception ->
            // Print the error stack trace to the log
            LogUtils.error(exception.stackTraceToString(), true)
            // Wait for 2000 milliseconds to ensure the log is written to the file
            Thread.sleep(2000)
            // Terminate the current process
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }
}