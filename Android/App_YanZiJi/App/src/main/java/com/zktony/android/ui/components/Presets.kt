package com.zktony.android.ui.components

import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService
import com.zktony.android.utils.Constants
import com.zktony.android.utils.ProductUtils
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
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val dataSaver = LocalDataSaver.current

    // 语言
    ResourceUtils.with(context.resources)
    val language = dataSaver.readData(Constants.LANGUAGE, Constants.DEFAULT_LANGUAGE)

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
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
    LogUtils.info("Presets", "Language: $language")

    // 提示音
    val promptSound = dataSaver.readData(Constants.PROMPT_SOUND, Constants.DEFAULT_PROMPT_SOUND)
    PromptSoundUtils.setPromptSound(promptSound)
    LogUtils.info("Presets", "Prompt sound: $promptSound")

    // P/N参数
    val pn = dataSaver.readData(Constants.PN, Constants.DEFAULT_PN)
    ProductUtils.ProductNumber = pn
    LogUtils.info("Presets", "P/N: $pn")

    content()
}