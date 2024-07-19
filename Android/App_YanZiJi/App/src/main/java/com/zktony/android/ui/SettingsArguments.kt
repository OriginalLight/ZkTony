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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Reply
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
import com.zktony.android.ui.components.ButtonLoading
import com.zktony.android.ui.components.ClearConfirmDialog
import com.zktony.android.ui.components.ImportConfirmDialog
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.zktyBrush
import com.zktony.android.ui.viewmodel.SettingsArgumentsViewModel
import kotlinx.coroutines.launch

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
    var importConformDialog by remember { mutableStateOf(false) }
    var clearConfirmDialog by remember { mutableStateOf(false) }
    var loadingExport by remember { mutableStateOf(false) }

    if (importConformDialog) {
        ImportConfirmDialog(
            items = viewModel.getArgumentFiles(),
            onDismiss = { importConformDialog = false },
            viewModel = viewModel
        )
    }

    if (clearConfirmDialog) {
        ClearConfirmDialog(
            onDismiss = { clearConfirmDialog = false },
            viewModel = viewModel
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(brush = zktyBrush, shape = MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .clickable { navigationActions.navigateUp() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.AutoMirrored.Default.Reply, contentDescription = "Back")
            Text(
                text = stringResource(id = R.string.arguments),
                style = MaterialTheme.typography.titleLarge
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { clearConfirmDialog = true }) {
                Text(text = stringResource(id = R.string.one_click_clear))
            }

            Button(onClick = { importConformDialog = true }) {
                Text(text = stringResource(id = R.string.one_click_import))
            }

            Button(onClick = {
                scope.launch {
                    loadingExport = true
                    viewModel.exportArguments()
                    loadingExport = false
                }
            }) {
                ButtonLoading(loading = loadingExport)
                Text(text = stringResource(id = R.string.one_click_export))
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
        SettingsRow(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { navigationActions.navigate(Route.SETTINGS_ARGUMENTS_EQUIPMENT) },
            title = stringResource(id = R.string.equipment_arguments)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }

        // 运行时参数
        SettingsRow(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { navigationActions.navigate(Route.SETTINGS_ARGUMENTS_RUNTIME) },
            title = stringResource(id = R.string.runtime_arguments)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }

        // 蠕动泵参数
        SettingsRow(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { navigationActions.navigate(Route.SETTINGS_ARGUMENTS_PUMP) },
            title = stringResource(id = R.string.pump_arguments)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }

        // 电压电流参数
        SettingsRow(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { navigationActions.navigate(Route.SETTINGS_ARGUMENTS_VOLTAGE) },
            title = stringResource(id = R.string.voltage_arguments)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }

        // 传感器参数
        SettingsRow(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable { navigationActions.navigate(Route.SETTINGS_ARGUMENTS_SENSOR) },
            title = stringResource(id = R.string.sensor_arguments)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "ArrowForwardIos"
            )
        }
    }
}