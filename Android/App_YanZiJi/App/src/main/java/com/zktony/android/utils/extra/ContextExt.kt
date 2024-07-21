package com.zktony.android.utils.extra

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.core.content.getSystemService
import java.util.Locale

@Suppress("DEPRECATION")
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