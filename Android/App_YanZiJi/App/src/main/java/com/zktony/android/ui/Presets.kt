package com.zktony.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.zktony.android.utils.Constants
import com.zktony.android.utils.ProductUtils
import com.zktony.android.utils.ResourceUtils
import com.zktony.android.utils.SoundUtils
import com.zktony.android.utils.extra.setLanguage
import com.zktony.datastore.LocalDataSaver

/**
 * A composable function that sets the application presets.
 * @param content The content to display.
 */
@Composable
fun Presets(content: @Composable () -> Unit) {

    val context = LocalContext.current
    val dataSaver = LocalDataSaver.current

    // 语言
    ResourceUtils.with(context.resources)
    context.setLanguage(dataSaver.readData(Constants.LANGUAGE, Constants.DEFAULT_LANGUAGE))

    // 提示音
    SoundUtils.with(dataSaver.readData(Constants.SOUND, Constants.DEFAULT_SOUND))

    // P/N参数
    ProductUtils.with(dataSaver.readData(Constants.PN, Constants.DEFAULT_PN))

    // view content
    content()
}