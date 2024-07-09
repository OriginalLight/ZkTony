package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.R
import com.zktony.android.data.Arguments
import com.zktony.android.data.ArgumentsClean
import com.zktony.android.data.ArgumentsTransfer
import com.zktony.android.ui.components.ArgumentsInputField
import com.zktony.android.ui.components.CircleTabRow
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.zktyBrush
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
        RuntimeContentRow(
            channel = channel,
            arguments = arguments,
            viewModel = viewModel
        )
    }
}

@Composable
fun SettingsArgumentsRuntimeTopBar(
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
fun RuntimeContentRow(
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
    ) { mutableStateOf(arguments[channel].inFillSpeed.toString()) }
    var inDrainSpeed by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inDrainSpeed.toString()) }
    var inFillTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inFillTime.toString()) }
    var inDrainTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].inDrainTime.toString()) }
    var outFillSpeed by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outFillSpeed.toString()) }
    var outDrainSpeed by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outDrainSpeed.toString()) }
    var outFillTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outFillTime.toString()) }
    var outDrainTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].outDrainTime.toString()) }
    var emptyTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].emptyTime.toString()) }
    var scale by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].scale.toString()) }
    var cleanInFillSpeed by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanInFillSpeed.toString()) }
    var cleanInDrainSpeed by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanInDrainSpeed.toString()) }
    var cleanInFillTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanInFillTime.toString()) }
    var cleanInDrainTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanInDrainTime.toString()) }
    var cleanOutFillSpeed by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanOutFillSpeed.toString()) }
    var cleanOutDrainSpeed by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanOutDrainSpeed.toString()) }
    var cleanOutFillTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanOutFillTime.toString()) }
    var cleanOutDrainTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanOutDrainTime.toString()) }
    var cleanEmptyTime by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanEmptyTime.toString()) }
    var cleanScale by remember(
        channel,
        arguments
    ) { mutableStateOf(arguments[channel].cleanScale.toString()) }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
            .clip(MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SettingsRow(title = stringResource(id = R.string.transfer)) {
                Button(onClick = {
                    scope.launch {
                        loading = true
                        try {
                            val transfer = ArgumentsTransfer(
                                inFillSpeed = inFillSpeed.toDoubleOrNull() ?: 0.0,
                                inDrainSpeed = inDrainSpeed.toDoubleOrNull() ?: 0.0,
                                inFillTime = inFillTime.toIntOrNull() ?: 0,
                                inDrainTime = inDrainTime.toIntOrNull() ?: 0,
                                outFillSpeed = outFillSpeed.toDoubleOrNull() ?: 0.0,
                                outDrainSpeed = outDrainSpeed.toDoubleOrNull() ?: 0.0,
                                outFillTime = outFillTime.toIntOrNull() ?: 0,
                                outDrainTime = outDrainTime.toIntOrNull() ?: 0,
                                emptyTime = emptyTime.toIntOrNull() ?: 0,
                                scale = scale.toDoubleOrNull() ?: 0.0,
                            )
                            viewModel.setTransferArguments(channel, transfer)
                        } finally {
                            loading = false
                        }
                    }
                }) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    Text(text = stringResource(id = R.string.save))
                }
            }
        }

        item {
            SettingsRow(title = "进液泵填充速度") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = inFillSpeed,
                    suffix = "mL/min"
                ) {
                    inFillSpeed = it

                }
            }
        }

        item {
            SettingsRow(title = "进液泵排液速度") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = inDrainSpeed,
                    suffix = "mL/min"
                ) {
                    inDrainSpeed = it
                }
            }
        }

        item {
            SettingsRow(title = "进液泵填充时间") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = inFillTime,
                    suffix = "min"
                ) {
                    inFillTime = it
                }
            }
        }

        item {
            SettingsRow(title = "进液泵排液时间") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = inDrainTime,
                    suffix = "min"
                ) {
                    inDrainTime = it
                }
            }
        }

        item {
            SettingsRow(title = "出液泵填充速度") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = outFillSpeed,
                    suffix = "mL/min"
                ) {
                    outFillSpeed = it
                }
            }
        }

        item {
            SettingsRow(title = "出液泵排液速度") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = outDrainSpeed,
                    suffix = "mL/min"
                ) {
                    outDrainSpeed = it
                }
            }
        }

        item {
            SettingsRow(title = "出液泵填充时间") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = outFillTime,
                    suffix = "min"
                ) {
                    outFillTime = it
                }
            }
        }

        item {
            SettingsRow(title = "出液泵排液时间") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = outDrainTime,
                    suffix = "min"
                ) {
                    outDrainTime = it
                }
            }
        }

        item {
            SettingsRow(title = "排空气时间") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = emptyTime,
                    suffix = "min"
                ) {
                    emptyTime = it
                }
            }
        }

        item {
            SettingsRow(title = "进出液速度比值") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = scale
                ) {
                    scale = it
                }
            }
        }

        item {
            SettingsRow(title = stringResource(id = R.string.clean)) {
                Button(onClick = {
                    scope.launch {
                        loading = true
                        try {
                            val clean = ArgumentsClean(
                                cleanInFillSpeed = cleanInFillSpeed.toDoubleOrNull() ?: 0.0,
                                cleanInDrainSpeed = cleanInDrainSpeed.toDoubleOrNull() ?: 0.0,
                                cleanInFillTime = cleanInFillTime.toIntOrNull() ?: 0,
                                cleanInDrainTime = cleanInDrainTime.toIntOrNull() ?: 0,
                                cleanOutFillSpeed = cleanOutFillSpeed.toDoubleOrNull() ?: 0.0,
                                cleanOutDrainSpeed = cleanOutDrainSpeed.toDoubleOrNull() ?: 0.0,
                                cleanOutFillTime = cleanOutFillTime.toIntOrNull() ?: 0,
                                cleanOutDrainTime = cleanOutDrainTime.toIntOrNull() ?: 0,
                                cleanEmptyTime = cleanEmptyTime.toIntOrNull() ?: 0,
                                cleanScale = cleanScale.toDoubleOrNull() ?: 0.0,
                            )
                            viewModel.setCleanArguments(channel, clean)
                        } finally {
                            loading = false
                        }
                    }
                }) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    Text(text = stringResource(id = R.string.save))
                }
            }
        }


        item {
            SettingsRow(title = "进液泵填充速度") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanInFillSpeed,
                    suffix = "mL/min"
                ) {
                    cleanInFillSpeed = it
                }
            }
        }

        item {
            SettingsRow(title = "进液泵排液速度") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanInDrainSpeed,
                    suffix = "mL/min"
                ) {
                    cleanInDrainSpeed = it
                }
            }
        }

        item {
            SettingsRow(title = "进液泵填充时间") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanInFillTime,
                    suffix = "min"
                ) {
                    cleanInFillTime = it
                }
            }
        }

        item {
            SettingsRow(title = "进液泵排液时间") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanInDrainTime,
                    suffix = "min"
                ) {
                    cleanInDrainTime = it
                }
            }
        }

        item {
            SettingsRow(title = "出液泵填充速度") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanOutFillSpeed,
                    suffix = "mL/min"
                ) {
                    cleanOutFillSpeed = it
                }
            }
        }

        item {
            SettingsRow(title = "出液泵排液速度") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanOutDrainSpeed,
                    suffix = "mL/min"
                ) {
                    cleanOutDrainSpeed = it
                }
            }
        }

        item {
            SettingsRow(title = "出液泵填充时间") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanOutFillTime,
                    suffix = "min"
                ) {
                    cleanOutFillTime = it
                }
            }
        }

        item {
            SettingsRow(title = "出液泵排液时间") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanOutDrainTime,
                    suffix = "min"
                ) {
                    cleanOutDrainTime = it
                }
            }
        }

        item {
            SettingsRow(title = "排空气时间") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanEmptyTime,
                    suffix = "min"
                ) {
                    cleanEmptyTime = it
                }
            }
        }

        item {
            SettingsRow(title = "进出液速度比值") {
                ArgumentsInputField(
                    modifier = Modifier.size(width = 350.dp, height = 48.dp),
                    value = cleanScale
                ) {
                    cleanScale = it
                }
            }
        }
    }
}