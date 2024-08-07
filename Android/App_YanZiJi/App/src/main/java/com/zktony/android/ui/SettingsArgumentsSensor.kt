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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.R
import com.zktony.android.data.Arguments
import com.zktony.android.data.ArgumentsBubble
import com.zktony.android.data.ChannelState
import com.zktony.android.ui.components.ArgumentsInputField
import com.zktony.android.ui.components.BaseTopBar
import com.zktony.android.ui.components.ButtonLoading
import com.zktony.android.ui.components.SegmentedButtonTabRow
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.SettingsArgumentsSensorViewModel
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.ProductUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsArgumentsSensorView(viewModel: SettingsArgumentsSensorViewModel = hiltViewModel()) {
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
        SettingsArgumentsSensorTopBar(
            channel = channel,
            onChannelChange = { channel = it },
            navigationActions = navigationActions
        )

        // 参数列表
        SensorArgumentsListView(
            channel = channel,
            arguments = arguments,
            channelStates = channelStates,
            viewModel = viewModel
        )

    }
}

// 顶部导航栏
@Composable
fun SettingsArgumentsSensorTopBar(
    modifier: Modifier = Modifier,
    channel: Int,
    onChannelChange: (Int) -> Unit,
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
                text = stringResource(id = R.string.app_sensor_arguments),
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

// 参数列表
@Composable
fun SensorArgumentsListView(
    modifier: Modifier = Modifier,
    channel: Int,
    arguments: List<Arguments>,
    channelStates: List<ChannelState>,
    viewModel: SettingsArgumentsSensorViewModel
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
            .clip(MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RealTimeSensorView(channel = channel, channelStates = channelStates)
        BubbleSensorThresholdView(channel = channel, arguments = arguments, viewModel = viewModel)
    }
}

// 实时参数-传感器
@Composable
fun RealTimeSensorView(
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
            text = "进液气泡传感器：" + if (channelState.bub1 == 0) "空气" else "液体",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            text = "出液气泡传感器：" + if (channelState.bub2 == 0) "空气" else "液体",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            text = "转膜盒光耦：" + if (channelState.opt2 == 0) "空闲" else "遮挡",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            text = "染色盒光耦：" + if (channelState.opt1 == 0) "空闲" else "遮挡",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// 气泡传感器阈值
@Composable
fun BubbleSensorThresholdView(
    modifier: Modifier = Modifier,
    channel: Int,
    arguments: List<Arguments>,
    viewModel: SettingsArgumentsSensorViewModel
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var inBobble by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inBubbleThreshold) }
    var outBobble by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outBubbleThreshold) }

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
        Text(text = "阈值设置", fontSize = 20.sp)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ArgumentsInputField(
                modifier = Modifier
                    .width(550.dp)
                    .height(48.dp)
                    .fillMaxWidth(),
                prefix = "出液",
                value = outBobble,
                onValueChange = { outBobble = it }
            )

            ArgumentsInputField(
                modifier = Modifier
                    .width(550.dp)
                    .height(48.dp)
                    .fillMaxWidth(),
                prefix = "进液",
                value = inBobble,
                onValueChange = { inBobble = it }
            )
        }

        Button(
            modifier = Modifier.width(120.dp),
            enabled = !loading,
            onClick = {
                scope.launch {
                    loading = true
                    viewModel.setSensorArguments(
                        channel = channel,
                        args = ArgumentsBubble(
                            inBubbleThreshold = inBobble,
                            outBubbleThreshold = outBobble
                        )
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