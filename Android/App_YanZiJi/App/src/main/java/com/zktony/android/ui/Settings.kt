package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.data.Sound
import com.zktony.android.data.Role
import com.zktony.android.ui.components.DateTimePicker
import com.zktony.android.ui.components.PasswordModifyDialog
import com.zktony.android.ui.components.RequirePermission
import com.zktony.android.ui.components.SegmentedButtonTabRow
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.SettingsViewModel
import com.zktony.android.utils.AuthUtils
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
            .fillMaxSize()
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
            UserSettingsView(viewModel = viewModel)
        }

        item {
            RequirePermission(role = Role.CUSTOMER_SERVICE) {
                FactorySettingsView()
            }
        }
    }
}

// 用户设置
@Composable
fun UserSettingsView(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel
) {
    val navigationActions = LocalNavigationActions.current
    var showPasswordModify by remember { mutableStateOf(false) }

    if (showPasswordModify) {
        PasswordModifyDialog(
            onDismiss = { showPasswordModify = false },
            onVerifyPassword = viewModel::verifyPassword,
            onModifyPassword = viewModel::modifyPassword
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(id = R.string.user_settings),
            style = MaterialTheme.typography.headlineMedium
        )

        // 当前用户
        AuthUtils.getLoggedUser()?.let {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = it.name,
                        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    )
                    Column {
                        Text(
                            text = it.role,
                            style = TextStyle(fontSize = 12.sp)
                        )
                        Text(
                            text = it.lastLoginTime.dateFormat("yyyy-MM-dd HH:mm"),
                            style = TextStyle(fontSize = 12.sp)
                        )
                    }
                }

                Button(onClick = { showPasswordModify = true }) {
                    Text(text = "修改密码")
                }
            }
        }

        RequirePermission(role = Role.ADMIN) {
            // 用户管理
            SettingsItem(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable { navigationActions.navigate(Route.SETTINGS_USER_MANAGEMENT) },
                title = stringResource(id = R.string.user_management)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                    contentDescription = "ArrowForwardIos"
                )
            }
        }
    }
}

// 系统设置
@Composable
fun SystemSettingsView(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel
) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    // 语言
    var language by rememberDataSaverState(
        key = Constants.LANGUAGE,
        default = Constants.DEFAULT_LANGUAGE
    )
    // 提示音
    var sound by rememberDataSaverState(
        key = Constants.SOUND,
        default = Constants.DEFAULT_SOUND
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
        SettingsItem(title = stringResource(id = R.string.language)) {
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
        SettingsItem(title = stringResource(id = R.string.sound)) {
            SegmentedButtonTabRow(
                modifier = Modifier.width(300.dp),
                tabItems = Sound.getResIdList().map { stringResource(id = it) },
                selected = Sound.indexFromName(sound)
            ) {
                scope.launch {
                    sound = Sound.getNameByIndex(it)
                    viewModel.setSound(Sound.getNameByIndex(it))
                }
            }
        }

        // 导航栏
        SettingsItem(title = stringResource(id = R.string.navigation_bar)) {
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
        SettingsItem(title = stringResource(id = R.string.status_bar)) {
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
        SettingsItem(title = stringResource(id = R.string.home_package)) {
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

        // 配件寿命
        SettingsItem(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { },
            title = stringResource(id = R.string.accessory_life)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }

        // 维护记录
        SettingsItem(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { },
            title = stringResource(id = R.string.maintenance_record)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }

        // 故障日志
        SettingsItem(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { },
            title = stringResource(id = R.string.error_log)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }

        // 版本信息
        SettingsItem(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { navigationActions.navigate(Route.SETTINGS_VERSION_INFO) },
            title = stringResource(id = R.string.version_info)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }

        // 系统时间
        SettingsItem(
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
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(id = R.string.factory_settings),
            style = MaterialTheme.typography.headlineMedium
        )

        // 参数
        SettingsItem(
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
        SettingsItem(
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

        RequirePermission(role = Role.FACTORY) {
            // Fqc
            SettingsItem(
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
            SettingsItem(
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

        // 运行日志
        SettingsItem(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { navigationActions.navigate(Route.SETTINGS_RUNTIME_LOG) },
            title = stringResource(id = R.string.runtime_log)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }
    }
}

@Composable
fun SettingsItem(
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