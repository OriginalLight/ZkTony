package com.zktony.core.ext

import android.annotation.SuppressLint
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.*
import android.os.Build
import android.os.LocaleList
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import java.io.File
import java.util.Locale

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
    val resources = this.resources
    val config = resources.configuration
    config.locale = Locale(language)
    config.setLocales(
        LocaleList(
            Locale(language)
        )
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getSystemService<LocaleManager>()?.applicationLocales = LocaleList(
            Locale(language)
        )
    }
    resources.updateConfiguration(config, resources.displayMetrics)
    return this.createConfigurationContext(config)
}

/**
 * 设置字体
 */
@SuppressLint("PrivateApi")
fun Context.initTypeface() {
    try {
        val field = Typeface::class.java.getDeclaredField("SANS_SERIF")
        field.isAccessible = true
        field.set(null, Typeface.createFromAsset(this.assets, "fonts/SFMono-Regular.otf"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}