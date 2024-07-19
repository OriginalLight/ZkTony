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
import com.zktony.android.data.ArgumentsSpeed
import com.zktony.android.data.PumpControl
import com.zktony.android.ui.components.ArgumentsInputField
import com.zktony.android.ui.components.ButtonLoading
import com.zktony.android.ui.components.CircleTabRow
import com.zktony.android.ui.components.RadioButtonGroup
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.zktyBrush
import com.zktony.android.ui.viewmodel.SettingsArgumentsPumpViewModel
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.ProductUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsArgumentsPumpView(viewModel: SettingsArgumentsPumpViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        // 拦截返回键
        navigationActions.navigateUp()
    }

    val arguments by AppStateUtils.argumentsList.collectAsStateWithLifecycle()
    var channel by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        SettingsArgumentsPumpTopBar(
            channel = channel,
            onChannelChange = { channel = it },
            navigationActions = navigationActions
        )
        // 内容
        PumpArgumentsListView(
            channel = channel,
            arguments = arguments,
            viewModel = viewModel
        )
    }
}

// 顶部导航栏
@Composable
fun SettingsArgumentsPumpTopBar(
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

// 内容
@Composable
fun PumpArgumentsListView(
    modifier: Modifier = Modifier,
    channel: Int,
    arguments: List<Arguments>,
    viewModel: SettingsArgumentsPumpViewModel
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
            .clip(MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PumpControlView(channel = channel, viewModel = viewModel)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                .clip(MaterialTheme.shapes.medium),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                InPumpCalibrationView(
                    channel = channel,
                    arguments = arguments,
                    viewModel = viewModel
                )
            }

            item {
                OutPumpCalibrationView(
                    channel = channel,
                    arguments = arguments,
                    viewModel = viewModel
                )
            }
        }
    }
}

// 蠕动泵控制
@Composable
fun PumpControlView(
    modifier: Modifier = Modifier,
    channel: Int,
    viewModel: SettingsArgumentsPumpViewModel
) {
    val scope = rememberCoroutineScope()
    var loadingStart by remember { mutableStateOf(false) }
    var loadingStop by remember { mutableStateOf(false) }
    // 进液/出液
    var inOrOut by remember { mutableIntStateOf(0) }
    // 正反转
    var direction by remember { mutableIntStateOf(0) }
    // 转速单位
    var speedUnit by remember { mutableIntStateOf(0) }
    // 转速
    var speed by remember { mutableStateOf("50") }
    // 时间
    var time by remember { mutableStateOf("60") }

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
        Text(text = "蠕动泵控制", fontSize = 20.sp)

        Column(
            modifier = Modifier.height(128.dp),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            RadioButtonGroup(
                selected = inOrOut, options = listOf("进液", "出液")
            ) {
                inOrOut = it
            }

            RadioButtonGroup(
                selected = direction, options = listOf("正转", "反转")
            ) {
                direction = it
            }

            RadioButtonGroup(
                selected = speedUnit,
                options = listOf("rpm", "mL/min")
            ) {
                speedUnit = it
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ArgumentsInputField(
                modifier = Modifier
                    .width(350.dp)
                    .height(56.dp)
                    .fillMaxWidth(),
                prefix = "转速",
                suffix = if (speedUnit == 0) "rpm" else "mL/min",
                value = speed,
                onValueChange = { speed = it }
            )

            ArgumentsInputField(
                modifier = Modifier
                    .width(350.dp)
                    .height(56.dp)
                    .fillMaxWidth(),
                prefix = "时间",
                suffix = "s",
                value = time,
                onValueChange = { time = it }
            )
        }

        Button(
            modifier = Modifier.width(120.dp),
            onClick = {
                scope.launch {
                    loadingStart = true
                    viewModel.startPump(
                        channel = channel,
                        control = PumpControl(
                            control = inOrOut,
                            direction = direction,
                            speedUnit = speedUnit,
                            speed = speed,
                            time = time
                        )
                    )
                    loadingStart = false
                }
            }
        ) {
            ButtonLoading(loading = loadingStart)
            Text(text = "开始", style = MaterialTheme.typography.bodyLarge)
        }

        OutlinedButton(
            modifier = Modifier.width(120.dp),
            onClick = {
                scope.launch {
                    loadingStop = true
                    viewModel.stopPump(
                        channel = channel,
                        control = inOrOut
                    )
                    loadingStop = false
                }
            }
        ) {
            ButtonLoading(loading = loadingStop)
            Text(text = "停止", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// 进液校准
@Composable
fun InPumpCalibrationView(
    modifier: Modifier = Modifier,
    channel: Int,
    arguments: List<Arguments>,
    viewModel: SettingsArgumentsPumpViewModel
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var s50 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inSpeedComp[0]) }
    var s100 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inSpeedComp[1]) }
    var s150 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inSpeedComp[2]) }
    var s200 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inSpeedComp[3]) }
    var s250 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inSpeedComp[4]) }
    var s300 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inSpeedComp[5]) }
    var s350 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inSpeedComp[6]) }
    var s400 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inSpeedComp[7]) }
    var s450 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inSpeedComp[8]) }
    var s500 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inSpeedComp[9]) }

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
        Text(text = "进液泵校准", fontSize = 20.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "50",
                    suffix = "mL",
                    value = s50
                ) {
                    s50 = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "150",
                    suffix = "mL",
                    value = s150
                ) {
                    s150 = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "250",
                    suffix = "mL",
                    value = s250
                ) {
                    s250 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "350",
                    suffix = "mL",
                    value = s350
                ) {
                    s350 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "450",
                    suffix = "mL",
                    value = s450
                ) {
                    s450 = it
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "100",
                    suffix = "mL",
                    value = s100
                ) {
                    s100 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "200",
                    suffix = "mL",
                    value = s200
                ) {
                    s200 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "300",
                    suffix = "mL",
                    value = s300
                ) {
                    s300 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "400",
                    suffix = "mL",
                    value = s400
                ) {
                    s400 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "500",
                    suffix = "mL",
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
                    loading = true
                    val inArgs =
                        listOf(s50, s100, s150, s200, s250, s300, s350, s400, s450, s500)
                    viewModel.setPumpArguments(
                        channel = channel,
                        args = ArgumentsSpeed(speedComp = inArgs)
                    )
                    loading = false
                }
            }
        ) {
            ButtonLoading(loading = loading)
            Text(text = "设置", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// 出液校准
@Composable
fun OutPumpCalibrationView(
    modifier: Modifier = Modifier,
    channel: Int,
    arguments: List<Arguments>,
    viewModel: SettingsArgumentsPumpViewModel
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var s50 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outSpeedComp[0]) }
    var s100 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outSpeedComp[1]) }
    var s150 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outSpeedComp[2]) }
    var s200 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outSpeedComp[3]) }
    var s250 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outSpeedComp[4]) }
    var s300 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outSpeedComp[5]) }
    var s350 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outSpeedComp[6]) }
    var s400 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outSpeedComp[7]) }
    var s450 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outSpeedComp[8]) }
    var s500 by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outSpeedComp[9]) }

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
        Text(text = "出液泵校准", fontSize = 20.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "50",
                    suffix = "mL",
                    value = s50
                ) {
                    s50 = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "150",
                    suffix = "mL",
                    value = s150
                ) {
                    s150 = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "250",
                    suffix = "mL",
                    value = s250
                ) {
                    s250 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "350",
                    suffix = "mL",
                    value = s350
                ) {
                    s350 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "450",
                    suffix = "mL",
                    value = s450
                ) {
                    s450 = it
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "100",
                    suffix = "mL",
                    value = s100
                ) {
                    s100 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "200",
                    suffix = "mL",
                    value = s200
                ) {
                    s200 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "300",
                    suffix = "mL",
                    value = s300
                ) {
                    s300 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "400",
                    suffix = "mL",
                    value = s400
                ) {
                    s400 = it
                }

                ArgumentsInputField(
                    modifier = Modifier.size(350.dp, 48.dp),
                    prefix = "500",
                    suffix = "mL",
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
                    loading = true
                    val outArgs =
                        listOf(s50, s100, s150, s200, s250, s300, s350, s400, s450, s500)
                    viewModel.setPumpArguments(
                        channel = channel,
                        args = ArgumentsSpeed(inOrOut = 1, speedComp = outArgs)
                    )
                    loading = false
                }
            }
        ) {
            ButtonLoading(loading = loading)
            Text(text = "设置", style = MaterialTheme.typography.bodyLarge)
        }
    }
}