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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.R
import com.zktony.android.data.PipelineControl
import com.zktony.android.ui.components.ArgumentsInputField
import com.zktony.android.ui.components.ButtonLoading
import com.zktony.android.ui.components.SegmentedButtonTabRow
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.zktyBrush
import com.zktony.android.ui.viewmodel.SettingsDebugPipelineViewModel
import com.zktony.android.utils.ProductUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsDebugPipelineView(viewModel: SettingsDebugPipelineViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigate(Route.SETTINGS)
    }

    var channel by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        SettingsDebugPipelineTopBar(
            channel = channel,
            onChannelChange = { channel = it },
            navigationActions = navigationActions
        )

        // 管路调试列表
        PipelineDebugListView(
            channel = channel,
            viewModel = viewModel
        )
    }
}

// 顶部导航栏
@Composable
fun SettingsDebugPipelineTopBar(
    modifier: Modifier = Modifier,
    channel: Int,
    onChannelChange: (Int) -> Unit,
    navigationActions: NavigationActions
) {
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
            Icon(
                imageVector = Icons.AutoMirrored.Default.Reply,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = stringResource(id = R.string.app_pipeline),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        SegmentedButtonTabRow(
            modifier = Modifier.width(400.dp),
            tabItems = List(ProductUtils.getChannelCount()) { stringResource(id = R.string.app_channel) + (it + 1) },
            selected = channel
        ) { index ->
            onChannelChange(index)
        }
    }
}

// 管路调试列表
@Composable
fun PipelineDebugListView(
    modifier: Modifier = Modifier,
    channel: Int,
    viewModel: SettingsDebugPipelineViewModel
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
        SettingsItem(title = "管路填充") {
            var selected by remember { mutableIntStateOf(0) }
            var loading by remember { mutableStateOf(false) }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SegmentedButtonTabRow(
                    modifier = Modifier.width(200.dp),
                    tabItems = listOf("转膜液", "清洗液"),
                    selected = selected
                ) {
                    selected = it
                }
                Button(
                    enabled = !loading,
                    onClick = {
                        scope.launch {
                            loading = true
                            viewModel.pipelineFill(channel, selected)
                            loading = false
                        }
                    }) {
                    ButtonLoading(loading = loading) {
                        Text(text = "开始", letterSpacing = 10.sp)
                    }
                }
            }
        }

        SettingsItem(title = "管路排空") {
            var selected by remember { mutableIntStateOf(0) }
            var loading by remember { mutableStateOf(false) }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SegmentedButtonTabRow(
                    modifier = Modifier.width(200.dp),
                    tabItems = listOf("转膜液", "清洗液"),
                    selected = selected
                ) {
                    selected = it
                }
                Button(
                    enabled = !loading,
                    onClick = {
                        scope.launch {
                            loading = true
                            viewModel.pipelineDrain(channel, selected)
                            loading = false
                        }
                    }) {
                    ButtonLoading(loading = loading) {
                        Text(text = "开始", letterSpacing = 10.sp)
                    }
                }
            }
        }

        SettingsItem(title = "管路清洗") {
            var speed by remember { mutableStateOf("100") }
            var time by remember { mutableStateOf("60") }
            var loading by remember { mutableStateOf(false) }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ArgumentsInputField(
                    modifier = Modifier
                        .width(350.dp)
                        .height(48.dp),
                    value = speed,
                    prefix = "速度",
                    suffix = "mL/min",
                ) {
                    speed = it
                }
                ArgumentsInputField(
                    modifier = Modifier
                        .width(350.dp)
                        .height(48.dp),
                    value = time,
                    prefix = "时间",
                    suffix = "s",
                ) {
                    time = it
                }

                Button(
                    enabled = !loading,
                    onClick = {
                        scope.launch {
                            loading = true
                            viewModel.pipelineClean(channel, PipelineControl(speed, time))
                            loading = false
                        }
                    }) {
                    ButtonLoading(loading = loading) {
                        Text(text = "开始", letterSpacing = 10.sp)
                    }
                }
            }
        }
    }
}