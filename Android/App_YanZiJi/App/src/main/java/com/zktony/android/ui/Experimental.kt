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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.drawscope.DrawStyle
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
import com.zktony.android.ui.components.ArgumentsInputField
import com.zktony.android.ui.components.ButtonLoading
import com.zktony.android.ui.components.ExperimentalState
import com.zktony.android.ui.components.ProgramSelectDialog
import com.zktony.android.ui.components.Tips
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.zktyHorizontalBrush
import com.zktony.android.ui.viewmodel.ExperimentalViewModel
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.ProductUtils
import com.zktony.android.utils.TipsUtils
import com.zktony.android.utils.extra.timeFormat
import com.zktony.room.entities.Program
import kotlinx.coroutines.launch

@Composable
fun ExperimentalView(viewModel: ExperimentalViewModel = hiltViewModel()) {

    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigateUp()
    }

    val entities = viewModel.entities.collectAsLazyPagingItems()
    val channelStateList by AppStateUtils.channelStateList.collectAsStateWithLifecycle()
    val channelProgramList by AppStateUtils.channelProgramList.collectAsStateWithLifecycle()
    val experimentalStateList by AppStateUtils.experimentalStateList.collectAsStateWithLifecycle()

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(ProductUtils.getChannelCount()) { index ->
            ExperimentalChannelView(
                modifier = Modifier.weight(1f),
                index = index,
                entities = entities,
                channelState = channelStateList[index],
                program = channelProgramList[index],
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
    channelState: ChannelState,
    program: Program,
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
                        cornerRadius = CornerRadius(16.dp.toPx()),
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
                    shape = MaterialTheme.shapes.medium
                )
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExperimentalProgramState(
                    index = index,
                    entities = entities,
                    program = program,
                    onProgramChange = { channel, program ->
                        viewModel.updateProgram(channel, program)
                    }
                )
                ExperimentalRealtimeState(channelState = channelState, viewModel = viewModel)
            }

            ExperimentalActions(
                index = index,
                program = program,
                channelState = channelState,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun ExperimentalProgramState(
    modifier: Modifier = Modifier,
    index: Int,
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
            Row(
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
                                showProgramSelectDialog = true
                            } else {
                                TipsUtils.showTips(Tips.warning("没有可用程序"))
                            }
                        }
                    }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = program.name,
                    style = TextStyle(
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "ArrowDown"
                )
            }
        }
        ExperimentalAttributeItem(
            title = when (program.workMode) {
                0 -> "电压"
                1 -> "电流"
                else -> "功率"
            }, subTitle = when (program.workMode) {
                0 -> "V"
                1 -> "A"
                else -> "W"
            }
        ) {
            ArgumentsInputField(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                value = program.value,
                shape = MaterialTheme.shapes.medium,
                showClear = false,
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            ) {
                onProgramChange(index, program.copy(value = it))
            }
        }
        if (program.experimentalType == 0) {
            ExperimentalAttributeItem(title = "流量", subTitle = "mL/min") {
                ArgumentsInputField(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    value = program.flowSpeed,
                    shape = MaterialTheme.shapes.medium,
                    showClear = false,
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                ) {
                    onProgramChange(index, program.copy(flowSpeed = it))
                }
            }
        }
        ExperimentalAttributeItem(title = "时间", subTitle = "min") {
            ArgumentsInputField(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                value = program.time,
                shape = MaterialTheme.shapes.medium,
                showClear = false,
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            ) {
                onProgramChange(index, program.copy(time = it))
            }
        }
    }
}

@Composable
fun ExperimentalRealtimeState(
    modifier: Modifier = Modifier,
    channelState: ChannelState,
    viewModel: ExperimentalViewModel
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "电流：",
                fontSize = 18.sp
            )
            Text(
                text = "${channelState.current} A",
                fontSize = 18.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "电压：",
                fontSize = 18.sp
            )
            Text(
                text = "${channelState.voltage} V",
                fontSize = 18.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "功率：",
                fontSize = 18.sp
            )
            Text(
                text = "${channelState.power} W",
                fontSize = 18.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "温度：",
                fontSize = 18.sp
            )
            Text(
                text = "${channelState.timing} ℃",
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun ExperimentalActions(
    modifier: Modifier = Modifier,
    index: Int,
    program: Program,
    channelState: ChannelState,
    viewModel: ExperimentalViewModel
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = (program.getTimeSeconds() - channelState.timing).timeFormat(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Button(
            enabled = !loading && program.canStart(),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    loading = true
                    viewModel.startExperiment(index, ExperimentalControl.fromProgram(program))
                    loading = false
                }
            }
        ) {
            ButtonLoading(loading = loading) {
                Text(text = "开 始", fontSize = 18.sp)
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
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
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