package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.R
import com.zktony.android.ui.components.SettingsArgumentsTopBar
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 顶部导航栏
        SettingsArgumentsTopBar(viewModel = viewModel)

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