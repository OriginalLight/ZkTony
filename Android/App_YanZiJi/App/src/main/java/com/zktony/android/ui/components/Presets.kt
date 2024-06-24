package com.zktony.android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.zktony.android.utils.Constants
import com.zktony.android.utils.PromptSoundUtils
import com.zktony.datastore.rememberDataSaverState
import java.util.Locale

/**
 * A composable function that sets the application presets.
 * @param content The content to display.
 */
@Suppress("DEPRECATION")
@Composable
fun Presets(content: @Composable () -> Unit) {
    // 语言
    val language by rememberDataSaverState(key = Constants.LANGUAGE, default = "zh")
    val configuration = LocalConfiguration.current
    val resource = LocalContext.current.resources

    val locale = Locale(language)
    Locale.setDefault(locale)
    configuration.setLocale(locale)
    resource.updateConfiguration(configuration, resource.displayMetrics)

    // 提示音
    val promptSound by rememberDataSaverState(key = Constants.PROMPT_SOUND, default = "mute")
    PromptSoundUtils.setPromptSound(promptSound)

    content()
}