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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.R
import com.zktony.android.data.entities.Calibration
import com.zktony.android.data.entities.Motor
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.internal.Point
import com.zktony.android.ui.CalibrationUiState
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.ui.utils.selectedColor
import com.zktony.android.utils.extra.dateFormat
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
    selected: Boolean,
    onClick: (Boolean) -> Unit
) {
    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(
                onClick = { onClick(false) },
                onDoubleClick = { onClick(true) }
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
        },
        colors = ListItemDefaults.colors(
            containerColor = selectedColor(selected)
        )
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PointItem(
    key: Int,
    item: Point,
    index: Int,
    uiState: CalibrationUiState,
    onClick: (Int) -> Unit,
    onPointChange: (Point) -> Unit,
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
                        .clickable { if (uiState.uiFlags == UiFlags.NONE) onClick(0) }
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    text = "加液",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (uiState.uiFlags == UiFlags.NONE) MaterialTheme.colorScheme.onSurface else Color.Gray
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgramItem(
    index: Int,
    item: Program,
    selected: Boolean,
    onClick: (Boolean) -> Unit
) {
    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(
                onClick = { onClick(false) },
                onDoubleClick = { onClick(true) }
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
        colors = ListItemDefaults.colors(
            containerColor = selectedColor(selected)
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MotorItem(
    item: Motor,
    selected: Boolean,
    onClick: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                color = selectedColor(selected),
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(
                onClick = { onClick(false) },
                onDoubleClick = { onClick(true) }
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