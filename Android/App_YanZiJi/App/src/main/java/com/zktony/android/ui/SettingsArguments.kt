package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.R
import com.zktony.android.ui.components.ImportConfirmDialog
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.SettingsArgumentsViewModel

@Composable
fun SettingsArgumentsView(viewModel: SettingsArgumentsViewModel = hiltViewModel()) {

    val navigationActions = LocalNavigationActions.current

    BackHandler {
        // 拦截返回键
        navigationActions.navigateUp()
    }

    Column {
        // 顶部导航栏
        SettingsArgumentsTopBar(navigationActions = navigationActions, viewModel = viewModel)
        // 参数列表
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsArgumentsTopBar(
    modifier: Modifier = Modifier,
    navigationActions: NavigationActions,
    viewModel: SettingsArgumentsViewModel
) {
    var showSecondConfirmation by remember { mutableStateOf(false) }

    if (showSecondConfirmation) {
        ImportConfirmDialog(
            onDismiss = { showSecondConfirmation = false },
            onConfirm = {
                viewModel.importArguments()
                showSecondConfirmation = false
            }
        )
    }

    TopAppBar(
        modifier = modifier,
        title = { Text(text = stringResource(id = R.string.arguments)) },
        navigationIcon = {
            IconButton(onClick = { navigationActions.navigateUp() }) {
                Icon(imageVector = Icons.AutoMirrored.Default.Reply, contentDescription = "Back")
            }
        },
        actions = {
            Row(
                modifier = Modifier.padding(end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { /*TODO*/ }) {
                    Text(text = stringResource(id = R.string.one_click_clear))
                }

                Button(onClick = { showSecondConfirmation = true }) {
                    Text(text = stringResource(id = R.string.one_click_import))
                }

                Button(onClick = { viewModel.exportArguments() }) {
                    Text(text = stringResource(id = R.string.one_click_export))
                }
            }
        }
    )
}