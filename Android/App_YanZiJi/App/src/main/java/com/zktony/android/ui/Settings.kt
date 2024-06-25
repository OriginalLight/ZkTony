package com.zktony.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.ui.components.RadioButtonGroup
import com.zktony.android.ui.components.Tips
import com.zktony.android.ui.components.TipsType
import com.zktony.android.utils.Constants
import com.zktony.android.utils.HzmctUtils
import com.zktony.android.utils.PromptSoundUtils
import com.zktony.android.utils.ResourceUtils
import com.zktony.android.utils.TipsUtils
import com.zktony.datastore.rememberDataSaverState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsView() {

    LazyColumn {
        item {
            SystemSettingsView()
        }
    }
}

// 用户设置
@Composable
fun UserSettingsView() {
    Column {
        Text(text = stringResource(id = R.string.user_settings))
    }
}

// 系统设置
@Composable
fun SystemSettingsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val scope = rememberCoroutineScope()
        // 语言
        var language by rememberDataSaverState(key = Constants.LANGUAGE, default = "zh")
        // 提示音
        var promptSound by rememberDataSaverState(key = Constants.PROMPT_SOUND, default = "mute")
        // 导航栏
        var navigationBar by rememberDataSaverState(key = Constants.NAVIGATION_BAR, default = false)
        // 状态栏
        var statusBar by rememberDataSaverState(key = Constants.STATUS_BAR, default = false)
        // 主屏幕
        var homeScreen by remember { mutableStateOf(HzmctUtils.getHomePackage() == BuildConfig.APPLICATION_ID) }

        Text(
            text = stringResource(id = R.string.system_settings),
            style = MaterialTheme.typography.headlineMedium
        )

        SettingsRaw(stringResource(id = R.string.language)) {
            RadioButtonGroup(
                selected = if (language == "zh") 0 else 1,
                options = listOf("简体中文", "English")
            ) {
                scope.launch {
                    language = if (it == 0) "zh" else "en"
                    TipsUtils.showTips(Tips(TipsType.INFO, "设置成功 重启应用生效"))
                }
            }
        }

        SettingsRaw(stringResource(id = R.string.prompt_sound)) {
            RadioButtonGroup(
                selected = PromptSoundUtils.getPromptSoundId(promptSound),
                options = listOf(
                    stringResource(id = R.string.mute),
                    stringResource(id = R.string.ring),
                    stringResource(id = R.string.voice)
                )
            ) {
                scope.launch {
                    promptSound = PromptSoundUtils.getPromptSoundStr(it)
                    PromptSoundUtils.setPromptSound(promptSound)
                    TipsUtils.showTips(
                        Tips(
                            TipsType.INFO,
                            "${ResourceUtils.stringResource(R.string.prompt_sound)} 设置成功"
                        )
                    )
                }
            }
        }

        SettingsRaw(stringResource(id = R.string.navigation_bar)) {
            Switch(checked = navigationBar, onCheckedChange = {
                scope.launch {
                    navigationBar = it
                    val bool = HzmctUtils.setNavigationBar(it)
                    if (bool) {
                        TipsUtils.showTips(Tips(TipsType.INFO, "设置成功"))
                    } else {
                        TipsUtils.showTips(Tips(TipsType.ERROR, "设置失败"))
                        delay(1000)
                        navigationBar = !navigationBar
                    }
                }
            })
        }

        SettingsRaw(stringResource(id = R.string.status_bar)) {
            Switch(checked = statusBar, onCheckedChange = {
                scope.launch {
                    statusBar = it
                    val bool = HzmctUtils.setStatusBar(it)
                    if (bool) {
                        TipsUtils.showTips(Tips(TipsType.INFO, "设置成功"))
                    } else {
                        TipsUtils.showTips(Tips(TipsType.ERROR, "设置失败"))
                        delay(1000)
                        statusBar = !statusBar
                    }
                }
            })
        }

        SettingsRaw(stringResource(id = R.string.home_screen)) {
            Switch(checked = homeScreen, onCheckedChange = {
                scope.launch {
                    homeScreen = it
                    val bool =
                        HzmctUtils.setHomePackage(if (it) BuildConfig.APPLICATION_ID else "com.android.launcher3")
                    if (bool) {
                        TipsUtils.showTips(Tips(TipsType.INFO, "设置成功"))
                    } else {
                        TipsUtils.showTips(Tips(TipsType.ERROR, "设置失败"))
                        delay(1000)
                        homeScreen = !homeScreen
                    }
                }
            })
        }
    }
}

// 工厂设置
@Composable
fun FactorySettingsView() {
    Column {
        Text(text = "工厂设置")
    }
}

@Composable
fun SettingsRaw(
    title: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
            .clip(MaterialTheme.shapes.small)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Preview
@Composable
fun PreviewSettingsView() {
    SettingsView()
}

@Preview
@Composable
fun PreviewUserSettingsView() {
    UserSettingsView()
}

@Preview
@Composable
fun PreviewSystemSettingsView() {
    SystemSettingsView()
}

@Preview(device = "spec:width=1280px,height=800px,dpi=440,orientation=portrait")
@Composable
fun PreviewFactorySettingsView() {
    FactorySettingsView()
}