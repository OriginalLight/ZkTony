package com.zktony.android.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.R
import com.zktony.android.data.entities.Curve
import com.zktony.android.data.entities.History
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.internal.Log
import com.zktony.android.data.entities.internal.Process
import com.zktony.android.data.entities.internal.ProcessType
import com.zktony.android.utils.extra.Point
import com.zktony.android.utils.extra.dateFormat
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/8/31 13:14
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CurveItem(
    modifier: Modifier = Modifier,
    index: Int,
    curve: Curve,
    onClick: (Curve) -> Unit,
    onDoubleClick: (Curve) -> Unit
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = { onClick(curve) },
                onDoubleClick = { onDoubleClick(curve) }
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${index + 1}、",
                style = MaterialTheme.typography.titleLarge,
                fontStyle = FontStyle.Italic,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = "M${curve.index}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = if (curve.enable) "启用中" else "未启用",
                style = MaterialTheme.typography.bodyMedium,
                color = if (curve.enable) MaterialTheme.colorScheme.onSurface else Color.Red
            )
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = curve.displayText,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = curve.createTime.dateFormat("yyyy/MM/dd"),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End,
            color = Color.Gray
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PointItem(
    point: Point,
    index: Int,
    onClickOne: () -> Unit,
    onClickTwo: () -> Unit,
    onPointChange: (Point) -> Unit
) {
    val scope = rememberCoroutineScope()
    val softKeyboard = LocalSoftwareKeyboardController.current
    val forceManager = LocalFocusManager.current
    var x by remember { mutableStateOf(point.x.toString()) }
    var y by remember { mutableStateOf(point.y.toString()) }

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
    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${index + 1}、",
                style = MaterialTheme.typography.titleMedium,
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onClickOne() }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = "测试",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onClickTwo() }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = stringResource(id = R.string.delete),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red
            )
        }
        BasicTextField(
            value = TextFieldValue(x, TextRange(x.length)),
            onValueChange = {
                scope.launch {
                    x = it.text
                    onPointChange(point.copy(x = it.text.toDoubleOrNull() ?: 0.0))
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
                            text = "出液",
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
                    onPointChange(point.copy(y = it.text.toDoubleOrNull() ?: 0.0))
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
                            text = "步数",
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
}


@Composable
fun HistoryItem(
    index: Int,
    history: History,
    onClick: (History) -> Unit,
) {
    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick(history) }
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${index + 1}、",
            style = MaterialTheme.typography.titleLarge,
            fontStyle = FontStyle.Italic,
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = history.createTime.dateFormat("yyyy/MM/dd"),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
fun LogItem(log: Log) {
    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = log.level,
                style = MaterialTheme.typography.bodyMedium,
                color = when (log.level) {
                    "DEBUG" -> Color.Blue
                    "INFO" -> Color.Green
                    "WARN" -> Color.Yellow
                    "ERROR" -> Color.Red
                    else -> Color.Gray
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = log.createTime.dateFormat("HH:mm:ss"),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End
            )
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = log.message,
            style = MaterialTheme.typography.bodyMedium
        )

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgramItem(
    modifier: Modifier = Modifier,
    index: Int,
    program: Program,
    onClick: (Program) -> Unit,
    onDoubleClick: (Program) -> Unit
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = { onClick(program) },
                onDoubleClick = { onDoubleClick(program) }
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${index + 1}、",
            style = MaterialTheme.typography.titleLarge,
            fontStyle = FontStyle.Italic,
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = program.displayText,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = program.createTime.dateFormat("yyyy/MM/dd"),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End,
            color = Color.Gray
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProcessItem(
    modifier: Modifier = Modifier,
    process: Process,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {

    val displayText = when (process.type) {
        ProcessType.BLOCKING -> stringResource(id = R.string.blocking)
        ProcessType.PRIMARY_ANTIBODY -> stringResource(id = R.string.primary_antibody)
        ProcessType.SECONDARY_ANTIBODY -> stringResource(id = R.string.secondary_antibody)
        ProcessType.WASHING -> stringResource(id = R.string.washing)
        ProcessType.PHOSPHATE_BUFFERED_SALINE -> stringResource(id = R.string.phosphate_buffered_saline)
    }

    val info: List<String> = mutableListOf<String>().apply {
        add("${process.temperature} ℃")
        add("${process.dosage} μL")
        add("${process.duration} ${if (process.type == ProcessType.WASHING) "Min" else "Hour"}")

        if (process.type == ProcessType.PRIMARY_ANTIBODY) {
            add("${'A' + process.origin}")
            add(if (process.recycle) "Recycle" else "No Recycle")
        }

        if (process.type == ProcessType.SECONDARY_ANTIBODY) {
            add("${'A' + process.origin}")
        }

        if (process.type == ProcessType.WASHING) {
            add("${process.times} Cycle")
        }
    }

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 16.dp),
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
                    .clickable { onDelete() }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = " 上 ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onDelete() }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = " 下 ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onDelete() }
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