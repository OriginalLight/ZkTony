package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.zktony.android.ui.components.SegmentedButtonTabRow
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.SettingsDebugSolenoidValveViewModel
import com.zktony.android.utils.ProductUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsDebugSolenoidValveView(viewModel: SettingsDebugSolenoidValveViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigateUp()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        SettingsDebugSolenoidValveTopBar(navigationActions = navigationActions)

        // 电磁阀调试列表
        SolenoidValveDebugListView(viewModel = viewModel)
    }
}

// 顶部导航栏
@Composable
fun SettingsDebugSolenoidValveTopBar(
    modifier: Modifier = Modifier,
    navigationActions: NavigationActions
) {
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
                text = stringResource(id = R.string.app_solenoid_valve),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

// 电磁阀调试列表
@Composable
fun SolenoidValveDebugListView(
    modifier: Modifier = Modifier,
    viewModel: SettingsDebugSolenoidValveViewModel
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
            .clip(MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(ProductUtils.getChannelCount()) { index ->
            SettingsItem(title = stringResource(id = R.string.`app_channel`) + (index + 1)) {
                var selected by remember { mutableIntStateOf(0) }
                SegmentedButtonTabRow(
                    modifier = Modifier.width(200.dp),
                    tabItems = listOf("转膜液", "清洗液"),
                    selected = selected
                ) {
                    scope.launch {
                        selected = it
                        if (!viewModel.setSolenoidValveState(index, it)) {
                            selected = if (selected == 0) 1 else 0
                        }
                    }
                }
            }
        }
    }
}