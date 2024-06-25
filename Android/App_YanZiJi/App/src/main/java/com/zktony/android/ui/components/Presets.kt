package com.zktony.android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.zktony.android.utils.Constants
import com.zktony.android.utils.PromptSoundUtils
import com.zktony.android.utils.ResourceUtils
import com.zktony.datastore.LocalDataSaver
import com.zktony.log.LogUtils
import java.util.Locale

/**
 * A composable function that sets the application presets.
 * @param content The content to display.
 */
@Suppress("DEPRECATION")
@Composable
fun Presets(content: @Composable () -> Unit) {
    // 数据存储
    val dataSaver = LocalDataSaver.current

    // 语言
    val language = dataSaver.readData(Constants.LANGUAGE, "zh")
    val configuration = LocalConfiguration.current
    val resource = LocalContext.current.resources

    val locale = Locale(language)
    Locale.setDefault(locale)
    configuration.setLocale(locale)
    resource.updateConfiguration(configuration, resource.displayMetrics)
    ResourceUtils.setLanguage(language)
    LogUtils.info("Presets", "Language: $language")

    // 提示音
    val promptSound = dataSaver.readData(Constants.PROMPT_SOUND, "mute")
    PromptSoundUtils.setPromptSound(promptSound)
    LogUtils.info("Presets", "Prompt sound: $promptSound")

    content()
}