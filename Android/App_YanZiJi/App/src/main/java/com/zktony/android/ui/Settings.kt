package com.zktony.android.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.data.PromptSound
import com.zktony.android.ui.components.DateTimePicker
import com.zktony.android.ui.components.SegmentedButtonTabRow
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.SettingsViewModel
import com.zktony.android.utils.Constants
import com.zktony.android.utils.HzmctUtils
import com.zktony.android.utils.extra.dateFormat
import com.zktony.datastore.rememberDataSaverState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun SettingsView(viewModel: SettingsViewModel = hiltViewModel()) {

    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigateUp()
    }

    LazyColumn(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
            .clip(MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SystemSettingsView(viewModel = viewModel)
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
        Text(text = stringResource(id = R.string.user_settings))
    }
}

// 系统设置
@Composable
fun SystemSettingsView(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel
) {

    val scope = rememberCoroutineScope()
    // 语言
    var language by rememberDataSaverState(
        key = Constants.LANGUAGE,
        default = Constants.DEFAULT_LANGUAGE
    )
    // 提示音
    var promptSound by rememberDataSaverState(
        key = Constants.PROMPT_SOUND,
        default = Constants.DEFAULT_PROMPT_SOUND
    )
    // 导航栏
    var navigationBar by rememberDataSaverState(
        key = Constants.NAVIGATION_BAR,
        default = Constants.DEFAULT_NAVIGATION_BAR
    )
    // 状态栏
    var statusBar by rememberDataSaverState(
        key = Constants.STATUS_BAR,
        default = Constants.DEFAULT_STATUS_BAR
    )
    // 主屏幕
    var homePackage by remember {
        mutableStateOf(
            HzmctUtils.getHomePackage()?.contains(BuildConfig.APPLICATION_ID) ?: false
        )
    }
    // 系统时间
    var systemTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDateTimePicker by remember { mutableStateOf(false) }

    // 显示日期时间选择器
    if (showDateTimePicker) {
        DateTimePicker(
            dateMillis = systemTime,
            onDateChange = {
                scope.launch {
                    systemTime = it
                    showDateTimePicker = false
                    if (!viewModel.setSystemTime(it)) {
                        delay(500L)
                        systemTime = System.currentTimeMillis()
                    }
                }
            }) {
            showDateTimePicker = false
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.system_settings),
            style = MaterialTheme.typography.headlineMedium
        )

        // 语言
        SettingsRow(title = stringResource(id = R.string.language)) {
            SegmentedButtonTabRow(
                modifier = Modifier.width(300.dp),
                tabItems = listOf("简体中文", "English"),
                selected = if (language == "zh") 0 else 1
            ) {
                scope.launch {
                    language = if (it == 0) "zh" else "en"
                    viewModel.setLanguage(if (it == 0) "zh" else "en")
                }
            }
        }

        // 提示音
        SettingsRow(title = stringResource(id = R.string.prompt_sound)) {
            SegmentedButtonTabRow(
                modifier = Modifier.width(300.dp),
                tabItems = PromptSound.getResIdList().map { stringResource(id = it) },
                selected = PromptSound.indexFromName(promptSound)
            ) {
                scope.launch {
                    promptSound = PromptSound.getNameByIndex(it)
                    viewModel.setPromptSound(PromptSound.getNameByIndex(it))
                }
            }
        }

        // 导航栏
        SettingsRow(title = stringResource(id = R.string.navigation_bar)) {
            Switch(
                checked = navigationBar,
                onCheckedChange = {
                    scope.launch {
                        navigationBar = it
                        if (!viewModel.setNavigationBar(it)) {
                            delay(500L)
                            navigationBar = !it
                        }
                    }
                }
            )
        }

        // 状态栏
        SettingsRow(title = stringResource(id = R.string.status_bar)) {
            Switch(
                checked = statusBar,
                onCheckedChange = {
                    scope.launch {
                        statusBar = it
                        if (!viewModel.setStatusBar(it)) {
                            delay(500L)
                            statusBar = !it
                        }
                    }
                }
            )
        }

        // 主屏幕
        SettingsRow(title = stringResource(id = R.string.home_package)) {
            Switch(checked = homePackage, onCheckedChange = {
                scope.launch {
                    homePackage = it
                    if (!viewModel.setHomePackage(it)) {
                        delay(500L)
                        homePackage = !it
                    }
                }
            })
        }

        // 系统时间
        SettingsRow(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { showDateTimePicker = true },
            title = stringResource(id = R.string.system_time)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = Date(systemTime).dateFormat("yyyy-MM-dd HH:mm"),
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                    contentDescription = "ArrowForwardIos"
                )
            }
        }
    }
}

// 工厂设置
@Composable
fun FactorySettingsView(modifier: Modifier = Modifier) {

    val navigationActions = LocalNavigationActions.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.factory_settings),
            style = MaterialTheme.typography.headlineMedium
        )

        // 参数
        SettingsRow(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { navigationActions.navigate(Route.SETTINGS_ARGUMENTS) },
            title = stringResource(id = R.string.arguments)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }

        // 调试
        SettingsRow(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { navigationActions.navigate(Route.SETTINGS_DEBUG) },
            title = stringResource(id = R.string.debug)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }

        // Fqc
        SettingsRow(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { navigationActions.navigate(Route.SETTINGS_FQC) },
            title = stringResource(id = R.string.fqc)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }

        // Aging
        SettingsRow(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { navigationActions.navigate(Route.SETTINGS_AGING) },
            title = stringResource(id = R.string.aging)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }
    }
}

@Composable
fun SettingsRow(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal))
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
    SystemSettingsView(viewModel = hiltViewModel())
}

@Preview(device = "spec:width=1280px,height=800px,dpi=440,orientation=portrait")
@Composable
fun PreviewFactorySettingsView() {
    FactorySettingsView()
}