package com.zktony.android.utils.extra

import android.annotation.SuppressLint
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.LocaleList
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import java.io.File
import java.util.Locale

@Suppress("DEPRECATION")
@SuppressLint("AppBundleLocaleChanges")
fun Context.setLanguage(language: String) {
    val config = resources.configuration
    config.setLocale(Locale(language))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        config.setLocales(LocaleList(Locale(language)))
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSystemService<LocaleManager>()?.applicationLocales = LocaleList(Locale(language))
    }
    resources.updateConfiguration(config, resources.displayMetrics)
}

/**
 * Installs an app by opening an APK file using the default app installation process.
 *
 * @param apk The APK file to be installed.
 */
fun Context.installApp(apk: File) {
    val type = "application/vnd.android.package-archive"
    val uri = FileProvider.getUriForFile(this, this.packageName + ".fileProvider", apk)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        addCategory(Intent.CATEGORY_DEFAULT)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        setDataAndType(uri, type)
    }
    startActivity(intent)
}