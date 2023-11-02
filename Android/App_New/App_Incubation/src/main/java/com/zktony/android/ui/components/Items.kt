package com.zktony.android.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.R
import com.zktony.android.data.entities.Calibration
import com.zktony.android.data.entities.History
import com.zktony.android.data.entities.Motor
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.internal.Log
import com.zktony.android.data.entities.internal.Point
import com.zktony.android.data.entities.internal.Process
import com.zktony.android.ui.utils.JobState
import com.zktony.android.ui.utils.selectedColor
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.timeFormat
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/8/31 13:14
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalibrationItem(
    index: Int,
    item: Calibration,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var delete by remember(item) { mutableStateOf(false) }

    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { delete = true },
            ),
        headlineContent = {
            Text(
                text = item.displayText,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = item.createTime.dateFormat("yyyy/MM/dd"),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        },
        leadingContent = {
            Text(
                text = "${index + 1}、",
                style = MaterialTheme.typography.headlineSmall,
                fontStyle = FontStyle.Italic
            )
        },
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        text = "M${item.index}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        text = if (item.enable) "生效中" else "未生效",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.enable) MaterialTheme.colorScheme.onSurface else Color.Red
                    )
                }
                if (delete) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.Red
                        )
                    }
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PointItem(
    key: Int,
    item: Point,
    index: Int,
    onClick: (Int) -> Unit,
    onPointChange: (Point) -> Unit
) {
    val scope = rememberCoroutineScope()
    val softKeyboard = LocalSoftwareKeyboardController.current
    val forceManager = LocalFocusManager.current
    var x by remember(key) { mutableStateOf(item.x.toString()) }
    var y by remember(key) { mutableStateOf(item.y.toString()) }

    val textStyle = TextStyle(
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        fontFamily = FontFamily.Monospace,
    )

    val keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done,
    )

    val keyboardActions = KeyboardActions(
        onDone = {
            softKeyboard?.hide()
            forceManager.clearFocus()
        }
    )
    ListItem(
        modifier = Modifier.clip(MaterialTheme.shapes.small),
        headlineContent = {
            Column(modifier = Modifier.padding(end = 16.dp)) {
                BasicTextField(
                    value = TextFieldValue(x, TextRange(x.length)),
                    onValueChange = {
                        scope.launch {
                            x = it.text
                            val value = it.text.toDoubleOrNull() ?: 0.0
                            if (value != item.x) {
                                onPointChange(item.copy(x = value))
                            }
                        }
                    },
                    textStyle = textStyle,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    decorationBox = @Composable { innerTextField ->
                        Column {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "向量",
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Serif,
                                    color = Color.Gray
                                )
                                innerTextField()
                            }
                            Divider()
                        }
                    }
                )

                BasicTextField(
                    value = TextFieldValue(y, TextRange(y.length)),
                    onValueChange = {
                        scope.launch {
                            y = it.text
                            val value = it.text.toDoubleOrNull() ?: 0.0
                            if (value != item.y) {
                                onPointChange(item.copy(y = value))
                            }
                        }
                    },
                    textStyle = textStyle,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    decorationBox = @Composable { innerTextField ->
                        Column {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "圈数",
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Serif,
                                    color = Color.Gray
                                )
                                innerTextField()
                            }
                            Divider()
                        }
                    }
                )
            }
        },
        leadingContent = {
            Text(
                text = "${index + 1}、",
                style = MaterialTheme.typography.headlineSmall,
                fontStyle = FontStyle.Italic
            )
        },
        trailingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small
                        )
                        .clip(MaterialTheme.shapes.small)
                        .clickable { onClick(0) }
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    text = "移动",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small
                        )
                        .clip(MaterialTheme.shapes.small)
                        .clickable { onClick(1) }
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    text = stringResource(id = R.string.delete),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
fun HistoryItem(
    index: Int,
    item: History,
    onClick: (History) -> Unit,
) {
    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick(item) },
        headlineContent = {
            Text(
                text = item.createTime.dateFormat("yyyy/MM/dd"),
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Text(
                text = item.createTime.dateFormat("HH:mm:ss"),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        },
        leadingContent = {
            Text(
                text = "${index + 1}、",
                style = MaterialTheme.typography.headlineSmall,
                fontStyle = FontStyle.Italic
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
fun LogItem(item: Log) {

    ListItem(
        modifier = Modifier.clip(MaterialTheme.shapes.small),
        headlineContent = {
            Text(
                text = item.createTime.dateFormat("HH:mm:ss"),
                style = MaterialTheme.typography.bodySmall
            )
        },
        supportingContent = {
            Text(
                modifier = Modifier.padding(8.dp),
                text = item.message,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Text(
                text = item.level,
                style = MaterialTheme.typography.bodyLarge,
                color = when (item.level) {
                    "DEBUG" -> Color.Blue
                    "INFO" -> Color.Green
                    "WARN" -> Color.Yellow
                    "ERROR" -> Color.Red
                    else -> Color.Gray
                }
            )
        },
        trailingContent = {
            Text(text = "${'A' + item.index}", style = MaterialTheme.typography.headlineMedium)
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgramItem(
    index: Int,
    item: Program,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var delete by remember(item) { mutableStateOf(false) }

    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { delete = true },
            ),
        headlineContent = {
            Text(
                text = item.displayText,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = item.createTime.dateFormat("yyyy/MM/dd"),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        },
        leadingContent = {
            Text(
                text = "${index + 1}、",
                style = MaterialTheme.typography.headlineSmall,
                fontStyle = FontStyle.Italic
            )
        },
        trailingContent = {
            if (delete) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.Red
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProcessItem(
    item: Process,
    selected: Boolean,
    onClick: (Int) -> Unit
) {

    val displayText = when (item.type) {
        Process.BLOCKING -> stringResource(id = R.string.blocking)
        Process.PRIMARY_ANTIBODY -> stringResource(id = R.string.primary_antibody)
        Process.SECONDARY_ANTIBODY -> stringResource(id = R.string.secondary_antibody)
        Process.WASHING -> stringResource(id = R.string.washing)
        Process.PHOSPHATE_BUFFERED_SALINE -> stringResource(id = R.string.phosphate_buffered_saline)
        else -> "未知"
    }

    val info: List<String> = mutableListOf<String>().apply {
        add("${item.temperature} ℃")
        add("${item.dosage} μL")
        add("${item.duration} ${if (item.type == Process.WASHING) "Min" else "Hour"}")

        if (item.type == Process.PRIMARY_ANTIBODY) {
            add("${'@' + item.origin}")
            add(if (item.recycle) "Recycle" else "No Recycle")
        }

        if (item.type == Process.SECONDARY_ANTIBODY) {
            add("${'@' + item.origin}")
        }

        if (item.type == Process.WASHING) {
            add("${item.times} Cycle")
        }
    }

    Column(
        modifier = Modifier
            .background(
                color = selectedColor(selected),
                shape = MaterialTheme.shapes.small
            )
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick(0) }
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onClick(1) }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = " ${stringResource(id = R.string.up)} ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onClick(2) }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = " ${stringResource(id = R.string.down)} ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onClick(3) }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = stringResource(id = R.string.delete),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red
            )
        }
        Divider()
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            info.forEach { text ->
                Text(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    text = text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProcessItem(item: Process) {

    val displayText = when (item.type) {
        Process.BLOCKING -> stringResource(id = R.string.blocking)
        Process.PRIMARY_ANTIBODY -> stringResource(id = R.string.primary_antibody)
        Process.SECONDARY_ANTIBODY -> stringResource(id = R.string.secondary_antibody)
        Process.WASHING -> stringResource(id = R.string.washing)
        Process.PHOSPHATE_BUFFERED_SALINE -> stringResource(id = R.string.phosphate_buffered_saline)
        else -> "未知"
    }

    val info: List<String> = mutableListOf<String>().apply {
        add("${item.temperature} ℃")
        add("${item.dosage} μL")
        add("${item.duration} ${if (item.type == Process.WASHING) "Min" else "Hour"}")

        if (item.type == Process.PRIMARY_ANTIBODY) {
            add("${'@' + item.origin}")
            add(if (item.recycle) "Recycle" else "No Recycle")
        }

        if (item.type == Process.SECONDARY_ANTIBODY) {
            add("${'@' + item.origin}")
        }

        if (item.type == Process.WASHING) {
            add("${item.times} Cycle")
        }
    }

    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = when (item.status) {
                    Process.UPCOMING -> "未开始"
                    Process.RUNNING -> "进行中"
                    Process.FINISHED -> "已完成"
                    else -> "未知"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when (item.status) {
                    Process.UPCOMING -> Color.Gray
                    Process.RUNNING -> Color.Green
                    Process.FINISHED -> Color.Blue
                    else -> Color.Red
                }
            )
        }
        Divider()
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            info.forEach { text ->
                Text(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    text = text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MotorItem(
    item: Motor,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var delete by remember(item) { mutableStateOf(false) }

    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { delete = true },
            ),
        headlineContent = {
            Text(
                text = item.displayText,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        supportingContent = {
            Text(
                text = "ADS - ${item.acceleration}/${item.deceleration}/${item.speed}",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingContent = {
            Text(
                text = "M ${item.index}",
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        trailingContent = {
            if (delete) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.Red
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
fun ModuleItem(
    index: Int,
    selected: Int,
    jobList: List<JobState>,
    insulation: List<Double>,
    onClick: () -> Unit
) {
    val jobState = jobList.find { it.index == index } ?: JobState()

    Box {
        Column(
            modifier = Modifier
                .sizeIn(minWidth = 196.dp, minHeight = 96.dp)
                .background(
                    color = selectedColor(index == selected),
                    shape = MaterialTheme.shapes.small
                )
                .clip(MaterialTheme.shapes.small)
                .clickable { onClick() }
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val message = when (jobState.status) {
                JobState.STOPPED -> "已就绪"
                JobState.RUNNING -> jobState.time.timeFormat()
                JobState.FINISHED -> "已完成"
                JobState.WAITING -> "等待中"
                JobState.LIQUID -> "加液中"
                JobState.WASTE -> "排液中"
                JobState.RECYCLE -> "回收中"
                else -> "未知"
            }

            Text(
                text = message,
                style = MaterialTheme.typography.headlineSmall,
                fontStyle = FontStyle.Italic
            )
        }

        Text(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset {
                    IntOffset(
                        x = -16.dp.roundToPx(),
                        y = -32.dp.roundToPx()
                    )
                },
            text = "${'A' + index}",
            style = TextStyle(
                fontSize = 64.sp,
                fontStyle = FontStyle.Italic,
                color = if (index == selected) Color.Blue else Color.Black
            )
        )

        Text(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset {
                    IntOffset(
                        x = -8.dp.roundToPx(),
                        y = 4.dp.roundToPx()
                    )
                },
            text = "${insulation.getOrNull(index + 1) ?: 0.0} ℃",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic
        )
    }
}