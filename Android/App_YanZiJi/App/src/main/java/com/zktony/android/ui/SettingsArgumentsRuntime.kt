package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import com.zktony.android.data.ArgumentsClean
import com.zktony.android.data.ArgumentsTransfer
import com.zktony.android.ui.components.ArgumentsInputField
import com.zktony.android.ui.components.ButtonLoading
import com.zktony.android.ui.components.SegmentedButtonTabRow
import com.zktony.android.ui.components.TopBarRow
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.SettingsArgumentsRuntimeViewModel
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.ProductUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsArgumentsRuntimeView(viewModel: SettingsArgumentsRuntimeViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        // 拦截返回键
        navigationActions.navigateUp()
    }

    val arguments by AppStateUtils.argumentsList.collectAsStateWithLifecycle()
    var channel by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        SettingsArgumentsRuntimeTopBar(
            channel = channel,
            onChannelChange = { channel = it },
            navigationActions = navigationActions
        )
        // 参数列表
        RuntimeArgumentsListView(
            channel = channel,
            arguments = arguments,
            viewModel = viewModel
        )
    }
}

// 顶部导航栏
@Composable
fun SettingsArgumentsRuntimeTopBar(
    modifier: Modifier = Modifier,
    channel: Int,
    onChannelChange: (Int) -> Unit,
    navigationActions: NavigationActions
) {
    TopBarRow(modifier = modifier) {
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

// 参数列表
@Composable
fun RuntimeArgumentsListView(
    modifier: Modifier = Modifier,
    channel: Int,
    arguments: List<Arguments>,
    viewModel: SettingsArgumentsRuntimeViewModel
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
            .clip(MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            TransferRuntimeArgumentsView(
                channel = channel,
                arguments = arguments,
                viewModel = viewModel
            )
        }

        item {
            CleanRuntimeArgumentsView(
                channel = channel,
                arguments = arguments,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun TransferRuntimeArgumentsView(
    modifier: Modifier = Modifier,
    channel: Int,
    arguments: List<Arguments>,
    viewModel: SettingsArgumentsRuntimeViewModel
) {
    // args
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var inFillSpeed by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inFillSpeed) }
    var inDrainSpeed by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inDrainSpeed) }
    var inFillTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inFillTime) }
    var inDrainTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inDrainTime) }
    var outFillSpeed by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outFillSpeed) }
    var outDrainSpeed by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outDrainSpeed) }
    var outFillTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outFillTime) }
    var outDrainTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outDrainTime) }
    var emptyTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].emptyTime) }
    var scale by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].scale) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(text = stringResource(id = R.string.app_transfer), fontSize = 20.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = inFillSpeed,
                    prefix = "进液泵填充速度",
                    suffix = "mL/min"
                ) {
                    inFillSpeed = it

                }
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = inFillTime,
                    prefix = "进液泵填充时间",
                    suffix = "s"
                ) {
                    inFillTime = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = outFillSpeed,
                    prefix = "出液泵填充速度",
                    suffix = "mL/min"
                ) {
                    outFillSpeed = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = outFillTime,
                    prefix = "出液泵填充时间",
                    suffix = "s"
                ) {
                    outFillTime = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = emptyTime,
                    prefix = "排空气时间",
                    suffix = "s"
                ) {
                    emptyTime = it
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = inDrainSpeed,
                    prefix = "进液泵排液速度",
                    suffix = "mL/min"
                ) {
                    inDrainSpeed = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = inDrainTime,
                    prefix = "进液泵排液时间",
                    suffix = "s"
                ) {
                    inDrainTime = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = outDrainSpeed,
                    prefix = "出液泵排液速度",
                    suffix = "mL/min"
                ) {
                    outDrainSpeed = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = outDrainTime,
                    prefix = "出液泵排液时间",
                    suffix = "s"
                ) {
                    outDrainTime = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = scale,
                    prefix = "进出液速度比值"
                ) {
                    scale = it
                }
            }
        }

        Button(
            modifier = Modifier.width(120.dp),
            enabled = !loading,
            onClick = {
                scope.launch {
                    loading = true
                    val transfer = ArgumentsTransfer(
                        inFillSpeed = inFillSpeed,
                        inDrainSpeed = inDrainSpeed,
                        inFillTime = inFillTime,
                        inDrainTime = inDrainTime,
                        outFillSpeed = outFillSpeed,
                        outDrainSpeed = outDrainSpeed,
                        outFillTime = outFillTime,
                        outDrainTime = outDrainTime,
                        emptyTime = emptyTime,
                        scale = scale
                    )
                    viewModel.setTransferArguments(channel, transfer)
                    loading = false
                }
            }) {
            ButtonLoading(loading = loading) {
                Text(
                    text = stringResource(id = R.string.app_set),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun CleanRuntimeArgumentsView(
    modifier: Modifier = Modifier,
    channel: Int,
    arguments: List<Arguments>,
    viewModel: SettingsArgumentsRuntimeViewModel
) {
    // args
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var cleanInFillSpeed by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanInFillSpeed) }
    var cleanInDrainSpeed by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanInDrainSpeed) }
    var cleanInFillTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanInFillTime) }
    var cleanInDrainTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanInDrainTime) }
    var cleanOutFillSpeed by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanOutFillSpeed) }
    var cleanOutDrainSpeed by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanOutDrainSpeed) }
    var cleanOutFillTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanOutFillTime) }
    var cleanOutDrainTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanOutDrainTime) }
    var cleanEmptyTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanEmptyTime) }
    var cleanScale by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanScale) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(text = stringResource(id = R.string.app_clean), fontSize = 20.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanInFillSpeed,
                    prefix = "进液泵填充速度",
                    suffix = "mL/min"
                ) {
                    cleanInFillSpeed = it

                }
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanInFillTime,
                    prefix = "进液泵填充时间",
                    suffix = "s"
                ) {
                    cleanInFillTime = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanOutFillSpeed,
                    prefix = "出液泵填充速度",
                    suffix = "mL/min"
                ) {
                    cleanOutFillSpeed = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanOutFillTime,
                    prefix = "出液泵填充时间",
                    suffix = "s"
                ) {
                    cleanOutFillTime = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanEmptyTime,
                    prefix = "排空气时间",
                    suffix = "s"
                ) {
                    cleanEmptyTime = it
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanInDrainSpeed,
                    prefix = "进液泵排液速度",
                    suffix = "mL/min"
                ) {
                    cleanInDrainSpeed = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanInDrainTime,
                    prefix = "进液泵排液时间",
                    suffix = "s"
                ) {
                    cleanInDrainTime = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanOutDrainSpeed,
                    prefix = "出液泵排液速度",
                    suffix = "mL/min"
                ) {
                    cleanOutDrainSpeed = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanOutDrainTime,
                    prefix = "出液泵排液时间",
                    suffix = "s"
                ) {
                    cleanOutDrainTime = it
                }
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanScale,
                    prefix = "进出液速度比值"
                ) {
                    cleanScale = it
                }
            }
        }

        Button(
            modifier = Modifier.width(120.dp),
            enabled = !loading,
            onClick = {
                scope.launch {
                    loading = true
                    val clean = ArgumentsClean(
                        cleanInFillSpeed = cleanInFillSpeed,
                        cleanInDrainSpeed = cleanInDrainSpeed,
                        cleanInFillTime = cleanInFillTime,
                        cleanInDrainTime = cleanInDrainTime,
                        cleanOutFillSpeed = cleanOutFillSpeed,
                        cleanOutDrainSpeed = cleanOutDrainSpeed,
                        cleanOutFillTime = cleanOutFillTime,
                        cleanOutDrainTime = cleanOutDrainTime,
                        cleanEmptyTime = cleanEmptyTime,
                        cleanScale = cleanScale,
                    )
                    viewModel.setCleanArguments(channel, clean)
                    loading = false
                }
            }) {
            ButtonLoading(loading = loading) {
                Text(
                    text = stringResource(id = R.string.app_set),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}