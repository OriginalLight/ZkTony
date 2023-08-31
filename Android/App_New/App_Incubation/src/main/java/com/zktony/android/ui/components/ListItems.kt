package com.zktony.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.data.entities.Curve
import com.zktony.android.ui.CurveUiEvent
import com.zktony.android.ui.CurveUiState
import com.zktony.android.utils.extra.Point
import com.zktony.android.utils.extra.dateFormat
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/8/31 13:14
 */
@Composable
fun CurveItem(
    modifier: Modifier = Modifier,
    index: Int,
    curve: Curve,
    uiEvent: (CurveUiEvent) -> Unit = {}
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable { uiEvent(CurveUiEvent.ToggleSelected(curve.id)) }
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "${index + 1}、",
            style = MaterialTheme.typography.titleLarge,
            fontStyle = FontStyle.Italic,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = curve.displayText,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "M${curve.index}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (curve.enable) MaterialTheme.colorScheme.primary else Color.Gray
        )

        Text(
            text = curve.createTime.dateFormat("yyyy/MM/dd"),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PointItem(
    point: Point,
    uiState: CurveUiState,
    onClickOne: (Point) -> Unit,
    onClickTwo: (Point) -> Unit,
    onPointChange: (Point) -> Unit
) {
    val scope = rememberCoroutineScope()
    val softKeyboard = LocalSoftwareKeyboardController.current
    val forceManager = LocalFocusManager.current
    var count by remember { mutableIntStateOf(0) }
    var x by remember { mutableStateOf(point.x.toString()) }
    var y by remember { mutableStateOf(point.y.toString()) }

    LaunchedEffect(key1 = point) {
        if (point.x != (x.toDoubleOrNull() ?: 0.0)) {
            x = point.x.toString()
        }
        if (point.y != (y.toDoubleOrNull() ?: 0.0)) {
            y = point.y.toString()
        }
    }

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

    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            BasicTextField(
                modifier = Modifier.width(256.dp),
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
                modifier = Modifier.width(256.dp),
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

        Spacer(modifier = Modifier.weight(1f))

        ElevatedButton(
            enabled = uiState.uiFlags == 0,
            onClick = { onClickOne(point) }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null
            )
        }

        ElevatedButton(onClick = {
            scope.launch {
                if (count == 0) {
                    count++
                } else {
                    count = 0
                    onClickTwo(point)
                }
            }
        }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = if (count == 0) MaterialTheme.colorScheme.primary else Color.Red
            )
        }
    }
}

@Preview
@Composable
fun CurveItemPreview() {
    CurveItem(index = 1, curve = Curve(displayText = "Test")) {}
}