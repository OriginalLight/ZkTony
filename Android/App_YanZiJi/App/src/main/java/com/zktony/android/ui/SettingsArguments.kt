package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.R
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.SettingsArgumentsViewModel
import com.zktony.android.utils.Constants
import com.zktony.datastore.rememberDataSaverState
import kotlinx.coroutines.delay

@Composable
fun SettingsArgumentsView(viewModel: SettingsArgumentsViewModel = hiltViewModel()) {

    val navigationActions = LocalNavigationActions.current

    BackHandler {
        // 拦截返回键
        navigationActions.navigate(Route.SETTINGS)
    }

    LazyColumn {
        item {
            EquipmentArgumentsView(viewModel = viewModel)
        }
        item {
            RuntimeArgumentsView(viewModel = viewModel)
        }
    }
}

// 设备参数
@Composable
fun EquipmentArgumentsView(
    modifier: Modifier = Modifier,
    viewModel: SettingsArgumentsViewModel
) {

    var sn by rememberDataSaverState(key = Constants.SN, initialValue = "Unknown")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.equipment_arguments),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

// 运行参数
@Composable
fun RuntimeArgumentsView(
    modifier: Modifier = Modifier,
    viewModel: SettingsArgumentsViewModel
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.runtime_arguments),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

// 蠕动泵参数
// 电压电流参数
// 传感器参数