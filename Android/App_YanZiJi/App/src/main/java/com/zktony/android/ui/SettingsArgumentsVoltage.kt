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
import com.zktony.android.data.Arguments
import com.zktony.android.data.ArgumentsCurrent
import com.zktony.android.data.ArgumentsTemperature
import com.zktony.android.data.ArgumentsVoltage
import com.zktony.android.data.ChannelState
import com.zktony.android.data.VoltageControl
import com.zktony.android.ui.components.ArgumentsInputField
import com.zktony.android.ui.components.ButtonLoading
import com.zktony.android.ui.components.SegmentedButtonTabRow
import com.zktony.android.ui.components.VerticalRadioButtonGroup
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.zktyBrush
import com.zktony.android.ui.viewmodel.SettingsArgumentsVoltageViewModel
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.ProductUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsArgumentsVoltageView(viewModel: SettingsArgumentsVoltageViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        // 拦截返回键
        navigationActions.navigateUp()
    }

    val channelStates by AppStateUtils.channelStateList.collectAsStateWithLifecycle()
    val arguments by AppStateUtils.argumentsList.collectAsStateWithLifecycle()
    var channel by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        SettingsArgumentsVoltageTopBar(
            channel = channel,
            onChannelChange = { channel = it },
            navigationActions = navigationActions
        )

        // 参数列表
        VoltageArgumentsListView(
            channel = channel,
            arguments = arguments,
            channelStates = channelStates,
            viewModel = viewModel
        )
    }
}

@Composable
fun SettingsArgumentsVoltageTopBar(
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
                text = stringResource(id = R.string.app_runtime_arguments),
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

@Composable
fun VoltageArgumentsListView(
    modifier: Modifier = Modifier,
    channel: Int,
    arguments: List<Arguments>,
    channelStates: List<ChannelState>,
    viewModel: SettingsArgumentsVoltageViewModel
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
            .clip(MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RealTimeVoltageView(channel = channel, channelStates = channelStates)
        VoltageControlView(channel = channel, viewModel = viewModel)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                .clip(MaterialTheme.shapes.medium),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                VoltageCalibrationView(
                    channel = channel,
                    arguments = arguments,
                    viewModel = viewModel
                )
            }

            item {
                CurrentCalibrationView(
                    channel = channel,
                    arguments = arguments,
                    viewModel = viewModel
                )
            }

            item {
                TemperatureCalibrationView(
                    channel = channel,
                    arguments = arguments,
                    viewModel = viewModel
                )
            }
        }
    }
}

// 实时参数-电压
@Composable
fun RealTimeVoltageView(
    modifier: Modifier = Modifier,
    channel: Int,
    channelStates: List<ChannelState>
) {
    val channelState = channelStates[channel]

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            text = "电压：" + channelState.voltage + " V",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            text = "电流：" + channelState.current + " A",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            text = "功率：" + channelState.power + " W",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            text = "温度：" + channelState.temperature + " ℃",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// 控制参数-电压
@Composable
fun VoltageControlView(
    modifier: Modifier = Modifier,
    channel: Int,
    viewModel: SettingsArgumentsVoltageViewModel
) {
    val scope = rememberCoroutineScope()
    var loadingStart by remember { mutableStateOf(false) }
    var loadingStop by remember { mutableStateOf(false) }
    var mode by remember { mutableIntStateOf(0) }
    var value by remember { mutableStateOf("0") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "电极控制", fontSize = 20.sp)

        VerticalRadioButtonGroup(
            selected = mode,
            options = listOf("恒压模式", "恒流模式", "恒功率模式")
        ) {
            mode = it
        }

        ArgumentsInputField(
            modifier = Modifier
                .width(350.dp)
                .height(56.dp),
            value = value,
            prefix = when (mode) {
                0 -> "电压"
                1 -> "电流"
                2 -> "功率"
                else -> "电压"
            },
            suffix = when (mode) {
                0 -> "V"
                1 -> "A"
                2 -> "W"
                else -> "V"
            }
        ) {
            value = it
        }

        Button(
            modifier = Modifier.width(120.dp),
            enabled = !loadingStart,
            onClick = {
                scope.launch {
                    loadingStart = true
                    val control = VoltageControl(
                        mode = mode,
                        voltage = if (mode == 0) value else "0",
                        current = if (mode == 1) value else "0",
                        power = if (mode == 2) value else "0"
                    )
                    viewModel.startVoltage(channel, control)
                    loadingStart = false
                }
            }
        ) {
            ButtonLoading(loading = loadingStart) {
                Text(text = "开始", style = MaterialTheme.typography.bodyLarge)
            }
        }

        OutlinedButton(
            enabled = !loadingStop,
            modifier = Modifier.width(120.dp),
            onClick = {
                scope.launch {
                    loadingStop = true
                    viewModel.stopVoltage(channel)
                    loadingStop = false
                }
            }
        ) {
            ButtonLoading(loading = loadingStop) {
                Text(text = "停止", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

// 电压校准
@Composable
fun VoltageCalibrationView(
    modifier: Modifier = Modifier,
    channel: Int,
    arguments: List<Arguments>,
    viewModel: SettingsArgumentsVoltageViewModel
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var voltComp by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].voltComp) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "电压校准", fontSize = 20.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (list in listOf(listOf(1, 3, 5, 7, 9), listOf(2, 4, 6, 8, 10))) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in list) {
                        ArgumentsInputField(
                            modifier = Modifier.size(350.dp, 48.dp),
                            prefix = "${i * 5}",
                            suffix = "V",
                            value = voltComp[i - 1]
                        ) {
                            voltComp = voltComp.toMutableList().also { list ->
                                list[i - 1] = it
                            }
                        }
                    }
                }
            }
        }

        Button(
            modifier = Modifier.width(120.dp),
            enabled = !loading,
            onClick = {
                scope.launch {
                    loading = true
                    viewModel.setVoltageArguments(
                        channel = channel,
                        args = ArgumentsVoltage(voltComp)
                    )
                    loading = false
                }
            }
        ) {
            ButtonLoading(loading = loading) {
                Text(text = "设置", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

// 电流校准
@Composable
fun CurrentCalibrationView(
    modifier: Modifier = Modifier,
    channel: Int,
    arguments: List<Arguments>,
    viewModel: SettingsArgumentsVoltageViewModel
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var currComp by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].currComp) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "电流校准", fontSize = 20.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (list in listOf(listOf(1, 3, 5, 7, 9), listOf(2, 4, 6, 8, 10))) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in list) {
                        ArgumentsInputField(
                            modifier = Modifier.size(350.dp, 48.dp),
                            prefix = "${i * 0.5}",
                            suffix = "A",
                            value = currComp[i - 1]
                        ) {
                            currComp = currComp.toMutableList().also { list ->
                                list[i - 1] = it
                            }
                        }
                    }
                }
            }
        }

        Button(
            modifier = Modifier.width(120.dp),
            enabled = !loading,
            onClick = {
                scope.launch {
                    loading = true
                    viewModel.setCurrentArguments(
                        channel = channel,
                        args = ArgumentsCurrent(currComp)
                    )
                    loading = false
                }
            }
        ) {
            ButtonLoading(loading = loading) {
                Text(text = "设置", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

// 温度校准
@Composable
fun TemperatureCalibrationView(
    modifier: Modifier = Modifier,
    channel: Int,
    arguments: List<Arguments>,
    viewModel: SettingsArgumentsVoltageViewModel
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var tempComp by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].tempComp) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "温度校准", fontSize = 20.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (list in listOf(listOf(1, 3, 5, 7, 9), listOf(2, 4, 6, 8, 10))) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in list) {
                        ArgumentsInputField(
                            modifier = Modifier.size(350.dp, 48.dp),
                            prefix = "${i * 10}",
                            suffix = "℃",
                            value = tempComp[i - 1]
                        ) {
                            tempComp = tempComp.toMutableList().also { list ->
                                list[i - 1] = it
                            }
                        }
                    }
                }
            }
        }

        Button(
            modifier = Modifier.width(120.dp),
            enabled = !loading,
            onClick = {
                scope.launch {
                    loading = true
                    viewModel.setTemperatureArguments(
                        channel = channel,
                        args = ArgumentsTemperature(tempComp)
                    )
                    loading = false
                }
            }
        ) {
            ButtonLoading(loading = loading) {
                Text(text = "设置", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}