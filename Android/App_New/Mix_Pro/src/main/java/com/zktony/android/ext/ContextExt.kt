package com.zktony.android.ext

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.LocaleManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.*
import android.os.Build
import android.os.LocaleList
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import java.io.File
import java.util.Locale
import kotlin.system.exitProcess

/**
 * 安装apk
 * @param apk apk文件
 */
fun Context.installApk(apk: File) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.addCategory(Intent.CATEGORY_DEFAULT)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val type = "application/vnd.android.package-archive"
    val uri: Uri = FileProvider.getUriForFile(
        this,
        this.packageName + ".fileProvider",
        apk
    )
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.setDataAndType(uri, type)
    this.startActivity(intent)
}

/**
 * 网络是否可用
 * @return Boolean
 */
@SuppressLint("MissingPermission")
fun Context.isNetworkAvailable(): Boolean {
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return true
        }
    } else {
        @Suppress("DEPRECATION")
        return cm.activeNetworkInfo?.isAvailable ?: false
    }
    return false
}


@Suppress("DEPRECATION")
fun Context.setLanguage(language: String): Context {
    if (language.isEmpty()) {
        return this
    }
    val resources = this.resources
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
        this.getSystemService<LocaleManager>()?.applicationLocales = LocaleList(
            Locale(language)
        )
    }
    resources.updateConfiguration(config, resources.displayMetrics)
    return this.createConfigurationContext(config)
}

fun Context.restartApp() {
    val intent = this.packageManager.getLaunchIntentForPackage(this.packageName)
    val pendingIntent = PendingIntent.getActivity(
        this, 0,
        intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
    )
    val mgr = this.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    mgr?.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent)
    exitProcess(0)
}