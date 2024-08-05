package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.data.ChannelState
import com.zktony.android.data.ExperimentalControl
import com.zktony.android.data.ExperimentalState
import com.zktony.android.data.disableEdit
import com.zktony.android.data.isRunning
import com.zktony.android.ui.components.ButtonLoading
import com.zktony.android.ui.components.ExperimentalAttributeInputField
import com.zktony.android.ui.components.ExperimentalState
import com.zktony.android.ui.components.ProgramSelectDialog
import com.zktony.android.ui.components.StopExperimentalDialog
import com.zktony.android.ui.components.Tips
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.zktyHorizontalBrush
import com.zktony.android.ui.viewmodel.ExperimentalViewModel
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.ProductUtils
import com.zktony.android.utils.TipsUtils
import com.zktony.android.utils.extra.timeFormat
import com.zktony.room.entities.Log
import com.zktony.room.entities.Program
import kotlinx.coroutines.launch

@Composable
fun ExperimentalView(viewModel: ExperimentalViewModel = hiltViewModel()) {

    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigateUp()
    }

    val entities = viewModel.entities.collectAsLazyPagingItems()
    val channelLogList by AppStateUtils.channelLogList.collectAsStateWithLifecycle()
    val channelStateList by AppStateUtils.channelStateList.collectAsStateWithLifecycle()
    val channelProgramList by AppStateUtils.channelProgramList.collectAsStateWithLifecycle()
    val experimentalStateList by AppStateUtils.experimentalStateList.collectAsStateWithLifecycle()

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(ProductUtils.getChannelCount()) { index ->
            ExperimentalChannelView(
                modifier = Modifier.weight(1f),
                index = index,
                entities = entities,
                channelLog = channelLogList[index],
                channelState = channelStateList[index],
                channelProgram = channelProgramList[index],
                experimentalState = experimentalStateList[index],
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun ExperimentalChannelView(
    modifier: Modifier = Modifier,
    index: Int = 0,
    entities: LazyPagingItems<Program>,
    channelLog: Log?,
    channelState: ChannelState,
    channelProgram: Program,
    experimentalState: ExperimentalState,
    viewModel: ExperimentalViewModel
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .drawWithCache {
                onDrawBehind {
                    drawRoundRect(
                        brush = zktyHorizontalBrush,
                        size = size.copy(height = 96.dp.toPx()),
                        cornerRadius = CornerRadius(16.dp.toPx())
                    )
                }
            }
    ) {

        ExperimentalChannelHeader(index = index, experimentalState = experimentalState)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(0.dp, 16.dp, 16.dp, 16.dp)
                )
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExperimentalProgramState(
                    index = index,
                    enable = !experimentalState.disableEdit(),
                    entities = entities,
                    program = channelProgram,
                    onProgramChange = { channel, program ->
                        viewModel.updateProgram(channel, program)
                    }
                )
                ExperimentalRealtimeState(channelState = channelState)
            }

            ExperimentalActions(
                index = index,
                log = channelLog,
                program = channelProgram,
                channelState = channelState,
                experimentalState = experimentalState,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun ExperimentalProgramState(
    modifier: Modifier = Modifier,
    index: Int,
    enable: Boolean,
    entities: LazyPagingItems<Program>,
    program: Program,
    onProgramChange: (Int, Program) -> Unit
) {
    val scope = rememberCoroutineScope()
    var showProgramSelectDialog by remember { mutableStateOf(false) }

    if (showProgramSelectDialog) {
        ProgramSelectDialog(entities = entities, onDismiss = { showProgramSelectDialog = false }) {
            showProgramSelectDialog = false
            onProgramChange(index, it)
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExperimentalAttributeItem(title = "程序", subTitle = "切换程序") {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    )
                    .clip(MaterialTheme.shapes.medium)
                    .clickable {
                        scope.launch {
                            if (entities.itemCount > 0) {
                                if (enable) {
                                    showProgramSelectDialog = true
                                } else {
                                    TipsUtils.showTips(Tips.warning("请先停止实验"))
                                }
                            } else {
                                TipsUtils.showTips(Tips.warning("没有可用程序"))
                            }
                        }
                    }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp),
                    text = program.name,
                    style = TextStyle(
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "ArrowDropDown"
                )
            }
        }
        ExperimentalAttributeItem(
            title = when (program.experimentalMode) {
                0 -> "电压"
                1 -> "电流"
                else -> "功率"
            }, subTitle = when (program.experimentalMode) {
                0 -> "V"
                1 -> "A"
                else -> "W"
            }
        ) {
            ExperimentalAttributeInputField(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                value = program.value,
                enable = enable
            ) {
                onProgramChange(index, program.copy(value = it))
            }
        }
        if (program.experimentalType == 0) {
            ExperimentalAttributeItem(title = "流量", subTitle = "mL/min") {
                ExperimentalAttributeInputField(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    value = program.flowSpeed,
                    enable = enable
                ) {
                    onProgramChange(index, program.copy(flowSpeed = it))
                }
            }
        }
        ExperimentalAttributeItem(title = "时间", subTitle = "min") {
            ExperimentalAttributeInputField(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                value = program.time,
                enable = enable
            ) {
                onProgramChange(index, program.copy(time = it))
            }
        }
    }
}

@Composable
fun ExperimentalRealtimeState(
    modifier: Modifier = Modifier,
    channelState: ChannelState
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExperimentalRealtimeItem(
                modifier = Modifier.weight(1f),
                title = "电流：", value = "${channelState.current}A"
            )
            ExperimentalRealtimeItem(
                modifier = Modifier.weight(1f),
                title = "电压：", value = "${channelState.voltage}V"
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExperimentalRealtimeItem(
                modifier = Modifier.weight(1f),
                title = "功率：", value = "${channelState.power}W"
            )
            ExperimentalRealtimeItem(
                modifier = Modifier.weight(1f),
                title = "温度：", value = "${channelState.time}℃"
            )
        }
    }
}

@Composable
fun ExperimentalActions(
    modifier: Modifier = Modifier,
    index: Int,
    log: Log?,
    program: Program,
    channelState: ChannelState,
    experimentalState: ExperimentalState,
    viewModel: ExperimentalViewModel
) {
    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    var loadingStart by remember { mutableStateOf(false) }
    var loadingPause by remember { mutableStateOf(false) }
    var loadingStop by remember { mutableStateOf(false) }
    var loadingResume by remember { mutableStateOf(false) }
    var showStopExperimentalDialog by remember { mutableStateOf(false) }

    if (showStopExperimentalDialog) {
        StopExperimentalDialog(onDismiss = { showStopExperimentalDialog = false }) {
            scope.launch {
                loadingStop = true
                viewModel.stopExperiment(index, program.experimentalType)
                loadingStop = false
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (experimentalState == ExperimentalState.TIMING) {
            Text(
                modifier = Modifier.padding(bottom = 24.dp),
                text = (program.timeSeconds() - channelState.time).timeFormat(),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (experimentalState.isRunning()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (experimentalState == ExperimentalState.PAUSE) {
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = !loadingResume,
                        onClick = {
                            scope.launch {
                                loadingResume = true
                                viewModel.startExperiment(
                                    index,
                                    ExperimentalControl.fromProgram(program)
                                )
                                loadingResume = false
                            }
                        }) {
                        ButtonLoading(loading = loadingResume) {
                            Text(text = "继续", fontSize = 18.sp)
                        }
                    }
                } else {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = !loadingPause,
                        onClick = {
                            scope.launch {
                                loadingPause = true
                                viewModel.pauseExperiment(index)
                                loadingPause = false
                            }
                        }) {
                        ButtonLoading(loading = loadingPause) {
                            Text(text = "暂停", fontSize = 18.sp)
                        }
                    }
                }

                Button(
                    modifier = Modifier.weight(1f),
                    enabled = !loadingStop,
                    onClick = {
                        showStopExperimentalDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    ButtonLoading(loading = loadingStop) {
                        Text(text = "停止", fontSize = 18.sp)
                    }
                }
            }
        }

        if (experimentalState == ExperimentalState.DRAIN) {
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = "正在排液，等等待！",
                fontSize = 18.sp
            )
        }

        if (experimentalState == ExperimentalState.READY) {

            log?.let {
                OutlinedButton(
                    modifier = Modifier.padding(bottom = 16.dp),
                    onClick = {
                        scope.launch {
                            navigationActions.navigate(Route.LOG_DETAIL + "/${it.id}")
                        }
                    }
                ) {
                    Text(text = "查看实验记录", fontSize = 18.sp)
                }
            }

            Button(
                enabled = !loadingStart && program.canStart(channelState.opt1, channelState.opt2),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {
                        loadingStart = true
                        viewModel.startExperiment(index, ExperimentalControl.fromProgram(program))
                        loadingStart = false
                    }
                }
            ) {
                ButtonLoading(loading = loadingStart) {
                    Text(text = "开 始", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun ExperimentalChannelHeader(
    modifier: Modifier = Modifier,
    index: Int = 0,
    experimentalState: ExperimentalState,
) {
    Box {
        Row(
            modifier = modifier
                .height(48.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ExperimentalState(state = experimentalState)
            Text(
                text = (index + 1).toString(),
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun ExperimentalAttributeItem(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(0.dp, 16.dp, 0.dp, 16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.width(64.dp)) {
            Text(text = title, fontSize = 18.sp)
            Text(text = subTitle, style = MaterialTheme.typography.bodySmall)
        }
        content()
    }
}

@Composable
fun ExperimentalRealtimeItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(0.dp, 16.dp, 0.dp, 16.dp)
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.align(Alignment.TopStart),
            text = title,
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            modifier = Modifier.offset(y = 4.dp),
            text = value,
            fontSize = 22.sp
        )
    }
}