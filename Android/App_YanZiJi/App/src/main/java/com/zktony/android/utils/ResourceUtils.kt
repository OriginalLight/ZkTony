package com.zktony.android.utils

import java.util.Locale

@Suppress("DEPRECATION")
object ResourceUtils {

    fun setLanguage(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = ApplicationUtils.ctx.resources.configuration
        configuration.setLocale(locale)
        ApplicationUtils.ctx.resources.updateConfiguration(
            configuration,
            ApplicationUtils.ctx.resources.displayMetrics
        )
    }

    fun stringResource(id: Int): String {
        return ApplicationUtils.ctx.getString(id)
    }
}