package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.R
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.SettingsArgumentsSensorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsArgumentsSensorView(viewModel: SettingsArgumentsSensorViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        // 拦截返回键
        navigationActions.navigateUp()
    }

    Column {
        // 顶部导航栏
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.sensor_arguments)) },
            navigationIcon = {
                IconButton(onClick = { navigationActions.navigateUp() }) {
                    Icon(imageVector = Icons.AutoMirrored.Default.Reply, contentDescription = "Back")
                }
            }
        )
    }
}