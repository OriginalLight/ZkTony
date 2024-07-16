package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.CircularProgressIndicator
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
import com.zktony.android.ui.components.CircleTabRow
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
            Icon(imageVector = Icons.AutoMirrored.Default.Reply, contentDescription = "Back")
            Text(
                text = stringResource(id = R.string.runtime_arguments),
                style = MaterialTheme.typography.titleLarge
            )
        }

        CircleTabRow(
            modifier = Modifier.size(400.dp, 48.dp),
            tabItems = List(ProductUtils.getChannelCount()) { stringResource(id = R.string.channel) + (it + 1) },
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
            onClick = {
                scope.launch {
                    try {
                        loadingStart = true
                        val control = VoltageControl(
                            mode = mode,
                            voltage = if (mode == 0) value else "0",
                            current = if (mode == 1) value else "0",
                            power = if (mode == 2) value else "0"
                        )
                        viewModel.startVoltage(channel, control)
                    } finally {
                        loadingStart = false
                    }
                }
            }
        ) {
            if (loadingStart) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text(text = "开始", style = MaterialTheme.typography.bodyLarge)
        }

        OutlinedButton(
            modifier = Modifier.width(120.dp),
            onClick = {
                scope.launch {
                    try {
                        loadingStop = true
                        viewModel.stopVoltage(channel)
                    } finally {
                        loadingStop = false
                    }
                }
            }
        ) {
            if (loadingStop) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text(text = "停止", style = MaterialTheme.typography.bodyLarge)
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
    var s50 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].voltComp[0]) }
    var s100 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].voltComp[1]) }
    var s150 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].voltComp[2]) }
    var s200 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].voltComp[3]) }
    var s250 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].voltComp[4]) }
    var s300 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].voltComp[5]) }
    var s350 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].voltComp[6]) }
    var s400 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].voltComp[7]) }
    var s450 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].voltComp[8]) }
    var s500 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].voltComp[9]) }

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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "5",
                    suffix = "V",
                    value = s50
                ) {
                    s50 = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "15",
                    suffix = "V",
                    value = s150
                ) {
                    s150 = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "25",
                    suffix = "V",
                    value = s250
                ) {
                    s250 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "35",
                    suffix = "V",
                    value = s350
                ) {
                    s350 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "45",
                    suffix = "V",
                    value = s450
                ) {
                    s450 = it
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "10",
                    suffix = "V",
                    value = s100
                ) {
                    s100 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "20",
                    suffix = "V",
                    value = s200
                ) {
                    s200 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "30",
                    suffix = "V",
                    value = s300
                ) {
                    s300 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "40",
                    suffix = "V",
                    value = s400
                ) {
                    s400 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "50",
                    suffix = "V",
                    value = s500
                ) {
                    s500 = it
                }
            }
        }

        Button(
            modifier = Modifier.width(120.dp),
            onClick = {
                scope.launch {
                    try {
                        loading = true
                        val args = listOf(s50, s100, s150, s200, s250, s300, s350, s400, s450, s500)
                        viewModel.setVoltageArguments(
                            channel = channel,
                            args = ArgumentsVoltage(
                                voltComp = args
                            )
                        )
                    } finally {
                        loading = false
                    }
                }
            }
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text(text = "设置", style = MaterialTheme.typography.bodyLarge)
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
    var s50 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].currComp[0]) }
    var s100 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].currComp[1]) }
    var s150 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].currComp[2]) }
    var s200 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].currComp[3]) }
    var s250 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].currComp[4]) }
    var s300 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].currComp[5]) }
    var s350 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].currComp[6]) }
    var s400 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].currComp[7]) }
    var s450 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].currComp[8]) }
    var s500 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].currComp[9]) }

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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "0.5",
                    suffix = "A",
                    value = s50
                ) {
                    s50 = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "1.5",
                    suffix = "A",
                    value = s150
                ) {
                    s150 = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "2.5",
                    suffix = "A",
                    value = s250
                ) {
                    s250 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "3.5",
                    suffix = "A",
                    value = s350
                ) {
                    s350 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "4.5",
                    suffix = "A",
                    value = s450
                ) {
                    s450 = it
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "1",
                    suffix = "A",
                    value = s100
                ) {
                    s100 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "2",
                    suffix = "A",
                    value = s200
                ) {
                    s200 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "3",
                    suffix = "A",
                    value = s300
                ) {
                    s300 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "4",
                    suffix = "A",
                    value = s400
                ) {
                    s400 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "5",
                    suffix = "A",
                    value = s500
                ) {
                    s500 = it
                }
            }
        }

        Button(
            modifier = Modifier.width(120.dp),
            onClick = {
                scope.launch {
                    try {
                        loading = true
                        val args = listOf(s50, s100, s150, s200, s250, s300, s350, s400, s450, s500)
                        viewModel.setCurrentArguments(
                            channel = channel,
                            args = ArgumentsCurrent(
                                currComp = args
                            )
                        )
                    } finally {
                        loading = false
                    }
                }
            }
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text(text = "设置", style = MaterialTheme.typography.bodyLarge)
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
    var s50 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].tempComp[0]) }
    var s100 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].tempComp[1]) }
    var s150 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].tempComp[2]) }
    var s200 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].tempComp[3]) }
    var s250 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].tempComp[4]) }
    var s300 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].tempComp[5]) }
    var s350 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].tempComp[6]) }
    var s400 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].tempComp[7]) }
    var s450 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].tempComp[8]) }
    var s500 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].tempComp[9]) }

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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "10",
                    suffix = "℃",
                    value = s50
                ) {
                    s50 = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "30",
                    suffix = "℃",
                    value = s150
                ) {
                    s150 = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "50",
                    suffix = "℃",
                    value = s250
                ) {
                    s250 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "70",
                    suffix = "℃",
                    value = s350
                ) {
                    s350 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "90",
                    suffix = "℃",
                    value = s450
                ) {
                    s450 = it
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "20",
                    suffix = "℃",
                    value = s100
                ) {
                    s100 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "40",
                    suffix = "℃",
                    value = s200
                ) {
                    s200 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "60",
                    suffix = "℃",
                    value = s300
                ) {
                    s300 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "80",
                    suffix = "℃",
                    value = s400
                ) {
                    s400 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "100",
                    suffix = "℃",
                    value = s500
                ) {
                    s500 = it
                }
            }
        }

        Button(
            modifier = Modifier.width(120.dp),
            onClick = {
                scope.launch {
                    try {
                        loading = true
                        val args = listOf(s50, s100, s150, s200, s250, s300, s350, s400, s450, s500)
                        viewModel.setTemperatureArguments(
                            channel = channel,
                            args = ArgumentsTemperature(
                                tempComp = args
                            )
                        )
                    } finally {
                        loading = false
                    }
                }
            }
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text(text = "设置", style = MaterialTheme.typography.bodyLarge)
        }
    }
}