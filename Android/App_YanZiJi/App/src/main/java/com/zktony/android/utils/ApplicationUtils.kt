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

    /**
     * Installs an app by opening an APK file using the default app installation process.
     *
     * @param apk The APK file to be installed.
     */
    fun installApp(apk: File) {
        val type = "application/vnd.android.package-archive"
        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileProvider", apk)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(uri, type)
        }
        ctx.startActivity(intent)
    }


    /**
     * Checks if the network is available.
     *
     * @return true if the network is available, false otherwise.
     */
    fun isNetworkAvailable(): Boolean {
        // Gets the ConnectivityManager system service from the application context.
        val cm = ctx.getSystemService(Application.CONNECTIVITY_SERVICE) as? ConnectivityManager
        // If it fails to obtain, it will directly return false.
            ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Acquires the network capabilities of the current network through the ConnectivityManager.
            val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            // If the network ability contains mobile network transmission ability, it returns true, indicating that the network is available.
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
                // If the network capabilities contain Wi-Fi transmission capabilities, it returns true, indicating that the network is available.
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
                // If the network capabilities contain ethernet transmission capabilities, it returns true, indicating that the network is available.
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true
            }
        } else {
            // If the Android version is earlier than API level Q, use an older API that has been deprecated.
            @Suppress("DEPRECATION")
            // Gets the network status information of the currently active network connection and returns whether it is available through the isAvailable attribute.
            return cm.activeNetworkInfo?.isAvailable ?: false
        }
        return false
    }

    @Suppress("DEPRECATION")
    fun setLanguage(context: Context, language: String): Context {
        val resources = context.resources
        val config = resources.configuration
        config.setLocale(Locale(language))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(
                LocaleList(
                    Locale(language)
                )
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService<LocaleManager>()?.applicationLocales = LocaleList(
                Locale(language)
            )
        }
        resources.updateConfiguration(config, resources.displayMetrics)
        return context.createConfigurationContext(config)
    }
}