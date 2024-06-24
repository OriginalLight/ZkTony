package com.zktony.android.ui.components.system

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.zktony.android.utils.Constants
import com.zktony.datastore.rememberDataSaverState
import java.util.Locale

@Suppress("DEPRECATION")
@Composable
fun Language(content: @Composable () -> Unit) {

    val language by rememberDataSaverState(key = Constants.LANGUAGE, default = "zh")
    val configuration = LocalConfiguration.current
    val resource = LocalContext.current.resources

    val locale = Locale(language)
    Locale.setDefault(locale)
    configuration.setLocale(locale)
    resource.updateConfiguration(configuration, resource.displayMetrics)

    content()
}