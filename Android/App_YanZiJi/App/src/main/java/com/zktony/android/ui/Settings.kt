package com.zktony.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.android.ui.components.RadioButtonGroup
import com.zktony.android.utils.Constants
import com.zktony.android.utils.HzmctUtils
import com.zktony.android.utils.SnackbarUtils
import com.zktony.datastore.rememberDataSaverState
import kotlinx.coroutines.launch

@Composable
fun SettingsView() {

    LazyColumn {
        item {
            UserSettingsView()
        }
        item {
            SystemSettingsView()
        }
        item {
            FactorySettingsView()
        }
    }
}

// 用户设置
@Composable
fun UserSettingsView() {
    Column {
        Text(text = "用户设置")
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
        var sound by rememberDataSaverState(key = Constants.PROMPT_SOUND, default = "mute")
        // 导航栏
        var navigationBar by rememberDataSaverState(key = Constants.NAVIGATION_BAR, default = false)
        // 状态栏
        var statusBar by rememberDataSaverState(key = Constants.STATUS_BAR, default = false)

        Text(text = "系统设置", style = MaterialTheme.typography.headlineMedium)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                .clip(MaterialTheme.shapes.small)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "系统语言", style = MaterialTheme.typography.titleMedium)
            RadioButtonGroup(
                selected = when (language) {
                    "zh" -> 0
                    "en" -> 1
                    else -> 0
                },
                options = listOf("简体中文", "English")
            ) {
                scope.launch {
                    language = when (it) {
                        0 -> "zh"
                        1 -> "en"
                        else -> "zh"
                    }
                    SnackbarUtils.showSnackbar("设置成功，重启应用生效。")
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                .clip(MaterialTheme.shapes.small)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "提示音", style = MaterialTheme.typography.titleMedium)
            RadioButtonGroup(
                selected = when (sound) {
                    "mute" -> 0
                    "ring" -> 1
                    "voice" -> 2
                    else -> 0
                },
                options = listOf("静音", "铃声", "语音")
            ) {
                scope.launch {
                    sound = when (it) {
                        0 -> "mute"
                        1 -> "ring"
                        2 -> "voice"
                        else -> "mute"
                    }
                    SnackbarUtils.showSnackbar("设置成功")
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                .clip(MaterialTheme.shapes.small)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "导航栏", style = MaterialTheme.typography.titleMedium)
            Switch(checked = navigationBar, onCheckedChange = {
                scope.launch {
                    navigationBar = it
                    HzmctUtils.setNavigationBar(it)
                    SnackbarUtils.showSnackbar("设置成功")
                }
            })
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                .clip(MaterialTheme.shapes.small)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "状态栏", style = MaterialTheme.typography.titleMedium)
            Switch(checked = statusBar, onCheckedChange = {
                scope.launch {
                    statusBar = it
                    HzmctUtils.setStatusBar(it)
                    SnackbarUtils.showSnackbar("设置成功")
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