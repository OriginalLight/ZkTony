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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.R
import com.zktony.android.data.ChannelState
import com.zktony.android.data.ExperimentalControl
import com.zktony.android.data.PipelineControl
import com.zktony.android.ui.components.ArgumentsInputField
import com.zktony.android.ui.components.ButtonLoading
import com.zktony.android.ui.components.SegmentedButtonTabRow
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.zktyBrush
import com.zktony.android.ui.viewmodel.SettingsDebugExperimentalViewModel
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.ProductUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsDebugExperimentalView(viewModel: SettingsDebugExperimentalViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigate(Route.SETTINGS)
    }

    var channel by remember { mutableIntStateOf(0) }
    val channelStates by AppStateUtils.channelStateList.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        SettingsDebugExperimentalTopBar(
            channel = channel,
            onChannelChange = { channel = it },
            navigationActions = navigationActions
        )

        // 实验调试列表
        ExperimentalDebugListView(
            channel = channel,
            channelStates = channelStates,
            viewModel = viewModel
        )
    }
}

// 顶部导航栏
@Composable
fun SettingsDebugExperimentalTopBar(
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
            Icon(imageVector = Icons.AutoMirrored.Default.Reply, contentDescription = "Back")
            Text(
                text = stringResource(id = R.string.experimental),
                style = MaterialTheme.typography.titleLarge
            )
        }

        SegmentedButtonTabRow(
            modifier = Modifier.size(400.dp, 48.dp),
            tabItems = List(ProductUtils.getChannelCount()) { stringResource(id = R.string.channel) + (it + 1) },
            selected = channel
        ) { index ->
            onChannelChange(index)
        }
    }
}

// 实验调试列表
@Composable
fun ExperimentalDebugListView(
    modifier: Modifier = Modifier,
    channel: Int,
    channelStates: List<ChannelState>,
    viewModel: SettingsDebugExperimentalViewModel
) {
    val scope = rememberCoroutineScope()
    var type by remember { mutableIntStateOf(0) }
    var mode by remember { mutableIntStateOf(0) }
    var value by remember { mutableStateOf("0") }
    var speed by remember { mutableStateOf("0") }
    var time by remember { mutableStateOf("0") }
    var temperature by remember { mutableStateOf("0") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
            .clip(MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 实时参数
        RealTimeExperimentalView(
            channel = channel,
            channelStates = channelStates
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // 实验类型
            item {
                SettingsRow(title = "实验类型") {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SegmentedButtonTabRow(
                            modifier = Modifier.width(200.dp),
                            tabItems = listOf("转膜", "染色"),
                            selected = type
                        ) {
                            type = it
                        }
                    }
                }
            }

            // 运行模式
            item {
                SettingsRow(title = "运行模式") {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SegmentedButtonTabRow(
                            modifier = Modifier.width(450.dp),
                            tabItems = listOf("恒压模式", "恒流模式", "恒功率模式"),
                            selected = mode
                        ) {
                            mode = it
                        }
                    }
                }
            }

            // 模式数值
            item {
                SettingsRow(title = if (mode == 0) "电压" else if (mode == 1) "电流" else "功率") {
                    ArgumentsInputField(
                        modifier = Modifier.size(350.dp, 48.dp),
                        value = value,
                        suffix = if (mode == 0) "V" else if (mode == 1) "A" else "W",
                        onValueChange = { value = it }
                    )
                }
            }

            // 补液速度
            if (type == 0) {
                item {
                    SettingsRow(title = "补液速度") {
                        ArgumentsInputField(
                            modifier = Modifier.size(350.dp, 48.dp),
                            value = speed,
                            suffix = "mL/min",
                            onValueChange = { speed = it }
                        )
                    }
                }
            }

            // 运行时间
            item {
                SettingsRow(title = "运行时间") {
                    ArgumentsInputField(
                        modifier = Modifier.size(350.dp, 48.dp),
                        value = time,
                        suffix = "s",
                        onValueChange = { time = it }
                    )
                }
            }

            // 最高温度
            item {
                SettingsRow(title = "最高温度") {
                    ArgumentsInputField(
                        modifier = Modifier.size(350.dp, 48.dp),
                        value = temperature,
                        suffix = "℃",
                        onValueChange = { temperature = it }
                    )
                }
            }

            // 开始实验
            item {
                SettingsRow(title = "实验操作") {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        var loadingStart by remember { mutableStateOf(false) }
                        var loadingStop by remember { mutableStateOf(false) }
                        Button(
                            enabled = !loadingStart,
                            onClick = {
                                scope.launch {
                                    loadingStart = true
                                    viewModel.startExperiment(
                                        channel,
                                        ExperimentalControl(
                                            type = type,
                                            mode = mode,
                                            speed = if (type == 0) speed else "0",
                                            time = time,
                                            voltage = if (mode == 0) value else "0",
                                            current = if (mode == 1) value else "0",
                                            power = if (mode == 2) value else "0",
                                            temperature = temperature
                                        )
                                    )
                                    loadingStart = false
                                }
                            }
                        ) {
                            ButtonLoading(loading = loadingStart) {
                                Text(text = "开始实验", style = MaterialTheme.typography.bodyLarge)
                            }
                        }

                        OutlinedButton(
                            enabled = !loadingStop,
                            onClick = {
                                scope.launch {
                                    loadingStop = true
                                    viewModel.stopExperiment(channel)
                                    loadingStop = false
                                }
                            }
                        ) {
                            ButtonLoading(loading = loadingStop) {
                                Text(text = "停止实验", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }

            // 数据导出
            item {
                SettingsRow(title = "数据导出") {
                    var loading by remember { mutableStateOf(false) }
                    Button(
                        enabled = !loading,
                        onClick = {
                            scope.launch {
                                loading = true
                                viewModel.exportCollecting()
                                loading = false
                            }
                        }) {
                        ButtonLoading(loading = loading) {
                            Text(text = "导出", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            // 管路清洗
            item {
                SettingsRow(title = "管路清洗") {
                    var speed1 by remember { mutableStateOf("100") }
                    var time1 by remember { mutableStateOf("60") }
                    var loading by remember { mutableStateOf(false) }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ArgumentsInputField(
                            modifier = Modifier
                                .width(350.dp)
                                .height(48.dp),
                            value = speed1,
                            prefix = "速度",
                            suffix = "mL/min",
                        ) {
                            speed1 = it
                        }

                        ArgumentsInputField(
                            modifier = Modifier
                                .width(350.dp)
                                .height(48.dp),
                            value = time1,
                            prefix = "时间",
                            suffix = "s",
                        ) {
                            time1 = it
                        }

                        Button(
                            enabled = !loading,
                            onClick = {
                                scope.launch {
                                    loading = true
                                    viewModel.pipelineClean(channel, PipelineControl(speed1, time1))
                                    loading = false
                                }
                            }) {
                            ButtonLoading(loading = loading) {
                                Text(text = "开始", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 实时参数-电压
@Composable
fun RealTimeExperimentalView(
    modifier: Modifier = Modifier,
    channel: Int,
    channelStates: List<ChannelState>
) {
    val channelState = channelStates[channel]
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                text = "电压：" + channelState.voltage + " V",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                text = "电流：" + channelState.current + " A",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                text = "功率：" + channelState.power + " W",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                text = "温度：" + channelState.temperature + " ℃",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                text = "时间：" + channelState.timing + " s",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                text = "进液气泡传感器：" + if (channelState.bubble1 == 0) "空气" else "液体",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                text = "出液气泡传感器：" + if (channelState.bubble2 == 0) "空气" else "液体",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                text = "染色盒光耦：" + if (channelState.opto1 == 0) "空闲" else "遮挡",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                text = "转膜盒光耦：" + if (channelState.opto2 == 0) "空闲" else "遮挡",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}