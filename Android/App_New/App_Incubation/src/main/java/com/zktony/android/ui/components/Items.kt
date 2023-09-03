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
import com.zktony.android.data.entities.Motor
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
    item: Curve,
    onClick: (Curve) -> Unit,
    onDoubleClick: (Curve) -> Unit
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = { onClick(item) },
                onDoubleClick = { onDoubleClick(item) }
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "# ${index + 1}",
                style = MaterialTheme.typography.titleLarge,
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.weight(1f))
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
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = item.displayText,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = item.createTime.dateFormat("yyyy/MM/dd"),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End,
            color = Color.Gray
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PointItem(
    item: Point,
    index: Int,
    onClickOne: () -> Unit,
    onClickTwo: () -> Unit,
    onPointChange: (Point) -> Unit
) {
    val scope = rememberCoroutineScope()
    val softKeyboard = LocalSoftwareKeyboardController.current
    val forceManager = LocalFocusManager.current
    var x by remember { mutableStateOf(item.x.toString()) }
    var y by remember { mutableStateOf(item.y.toString()) }

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
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "# ${index + 1}",
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
                text = "加液测试",
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
        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
            BasicTextField(
                value = TextFieldValue(x, TextRange(x.length)),
                onValueChange = {
                    scope.launch {
                        x = it.text
                        onPointChange(item.copy(x = it.text.toDoubleOrNull() ?: 0.0))
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
                        onPointChange(item.copy(y = it.text.toDoubleOrNull() ?: 0.0))
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
}


@Composable
fun HistoryItem(
    index: Int,
    item: History,
    onClick: (History) -> Unit,
) {
    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick(item) }
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "# ${index + 1}",
            style = MaterialTheme.typography.titleLarge,
            fontStyle = FontStyle.Italic
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = item.createTime.dateFormat("yyyy/MM/dd"),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
fun LogItem(item: Log) {
    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = item.createTime.dateFormat("HH:mm:ss"),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = item.level,
                style = MaterialTheme.typography.bodyMedium,
                color = when (item.level) {
                    "DEBUG" -> Color.Blue
                    "INFO" -> Color.Green
                    "WARN" -> Color.Yellow
                    "ERROR" -> Color.Red
                    else -> Color.Gray
                }
            )

        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = item.message,
            style = MaterialTheme.typography.bodyLarge
        )

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgramItem(
    modifier: Modifier = Modifier,
    index: Int,
    item: Program,
    onClick: (Program) -> Unit,
    onDoubleClick: (Program) -> Unit
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = { onClick(item) },
                onDoubleClick = { onDoubleClick(item) }
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "# ${index + 1}",
            style = MaterialTheme.typography.titleLarge,
            fontStyle = FontStyle.Italic
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = item.displayText,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = item.createTime.dateFormat("yyyy/MM/dd"),
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
    item: Process,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onUpOrDown: (Boolean) -> Unit
) {

    val displayText = when (item.type) {
        ProcessType.BLOCKING -> stringResource(id = R.string.blocking)
        ProcessType.PRIMARY_ANTIBODY -> stringResource(id = R.string.primary_antibody)
        ProcessType.SECONDARY_ANTIBODY -> stringResource(id = R.string.secondary_antibody)
        ProcessType.WASHING -> stringResource(id = R.string.washing)
        ProcessType.PHOSPHATE_BUFFERED_SALINE -> stringResource(id = R.string.phosphate_buffered_saline)
    }

    val info: List<String> = mutableListOf<String>().apply {
        add("${item.temperature} ℃")
        add("${item.dosage} μL")
        add("${item.duration} ${if (item.type == ProcessType.WASHING) "Min" else "Hour"}")

        if (item.type == ProcessType.PRIMARY_ANTIBODY) {
            add("${'A' + item.origin}")
            add(if (item.recycle) "Recycle" else "No Recycle")
        }

        if (item.type == ProcessType.SECONDARY_ANTIBODY) {
            add("${'A' + item.origin}")
        }

        if (item.type == ProcessType.WASHING) {
            add("${item.times} Cycle")
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
                    .clickable { onUpOrDown(true) }
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
                    .clickable { onUpOrDown(false) }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MotorItem(
    modifier: Modifier = Modifier,
    item: Motor,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = { onClick() },
                onDoubleClick = { onDoubleClick() }
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "M ${item.index}",
            fontSize = 50.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = "A - ${item.acceleration}",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "D - ${item.deceleration}",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "S - ${item.speed}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}