package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.R
import com.zktony.android.ui.components.BaseTopBar
import com.zktony.android.ui.components.FileChoiceDialog
import com.zktony.android.ui.components.IconLoading
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.SettingsArgumentsViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingsArgumentsView(viewModel: SettingsArgumentsViewModel = hiltViewModel()) {

    val navigationActions = LocalNavigationActions.current

    BackHandler {
        // 拦截返回键
        navigationActions.navigateUp()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        SettingsArgumentsTopBar(navigationActions = navigationActions, viewModel = viewModel)
        // 参数列表
        ArgumentsListView(navigationActions = navigationActions)
    }
}

// 顶部导航栏
@Composable
fun SettingsArgumentsTopBar(
    modifier: Modifier = Modifier,
    navigationActions: NavigationActions,
    viewModel: SettingsArgumentsViewModel
) {
    val scope = rememberCoroutineScope()
    var showFileChoice by remember { mutableStateOf(false) }
    var fileObjectList by remember { mutableStateOf(listOf<File>()) }
    var loadingExport by remember { mutableStateOf(false) }
    var loadingImport by remember { mutableStateOf(false) }

    if (showFileChoice) {
        FileChoiceDialog(files = fileObjectList, onDismiss = { showFileChoice = false }) { file ->
            scope.launch {
                loadingImport = true
                showFileChoice = false
                viewModel.import(file)
                loadingImport = false
            }
        }
    }

    BaseTopBar(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .clickable { navigationActions.navigateUp() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.Reply,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = stringResource(id = R.string.app_arguments),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                enabled = !loadingImport,
                onClick = {
                    scope.launch {
                        viewModel.getArgumentFiles()?.let {
                            fileObjectList = it
                            showFileChoice = true
                        }
                    }
                }) {
                IconLoading(loading = loadingImport) {
                    Icon(
                        imageVector = Icons.Default.ImportExport,
                        contentDescription = "ImportExport"
                    )
                }
                Text(
                    text = stringResource(id = R.string.app_import),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                enabled = !loadingExport,
                onClick = {
                    scope.launch {
                        loadingExport = true
                        viewModel.export()
                        loadingExport = false
                    }
                }) {
                IconLoading(loading = loadingExport) {
                    Icon(
                        imageVector = Icons.Default.ImportExport,
                        contentDescription = "ImportExport"
                    )
                }
                Text(
                    text = stringResource(id = R.string.app_export),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

// 参数列表
@Composable
fun ArgumentsListView(
    modifier: Modifier = Modifier,
    navigationActions: NavigationActions
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
            .clip(MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 设备参数
        SettingsItem(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { navigationActions.navigate(Route.SETTINGS_ARGUMENTS_EQUIPMENT) },
            title = stringResource(id = R.string.app_equipment_arguments)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }

        // 运行时参数
        SettingsItem(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { navigationActions.navigate(Route.SETTINGS_ARGUMENTS_RUNTIME) },
            title = stringResource(id = R.string.app_runtime_arguments)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }

        // 蠕动泵参数
        SettingsItem(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { navigationActions.navigate(Route.SETTINGS_ARGUMENTS_PUMP) },
            title = stringResource(id = R.string.app_pump_arguments)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }

        // 电压电流参数
        SettingsItem(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { navigationActions.navigate(Route.SETTINGS_ARGUMENTS_VOLTAGE) },
            title = stringResource(id = R.string.app_voltage_arguments)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }

        // 传感器参数
        SettingsItem(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { navigationActions.navigate(Route.SETTINGS_ARGUMENTS_SENSOR) },
            title = stringResource(id = R.string.app_sensor_arguments)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }
    }
}