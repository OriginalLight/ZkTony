package com.zktony.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.data.entities.Curve
import com.zktony.android.ui.CurveUiEvent
import com.zktony.android.ui.CurveUiState
import com.zktony.android.utils.extra.Point
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/8/31 14:38
 */

@Composable
fun CurveListHeader(
    uiState: CurveUiState,
    uiEvent: (CurveUiEvent) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var count by remember { mutableIntStateOf(0) }
    var dialog by remember { mutableStateOf(false) }

    if (dialog) {
        InputDialog(
            onConfirm = {
                uiEvent(CurveUiEvent.Insert(it))
                dialog = false
            },
            onCancel = { dialog = false }
        )
    }

    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small
            )
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(imageVector = Icons.Default.Analytics, contentDescription = null)
        Spacer(modifier = Modifier.weight(1f))
        AnimatedVisibility(visible = uiState.selected != 0L) {
            ElevatedButton(onClick = {
                scope.launch {
                    if (count == 0) {
                        count++
                    } else {
                        count = 0
                        uiEvent(CurveUiEvent.Delete(uiState.selected))
                        uiEvent(CurveUiEvent.ToggleSelected(0L))
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
        ElevatedButton(onClick = { dialog = true }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CurvePointHeader(
    curve: Curve,
    uiEvent: (CurveUiEvent) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val softKeyboard = LocalSoftwareKeyboardController.current
    val forceManager = LocalFocusManager.current

    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small
            )
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BasicTextField(
            modifier = Modifier.width(128.dp),
            value = TextFieldValue(
                curve.index.toString(),
                TextRange(curve.index.toString().length)
            ),
            onValueChange = {
                scope.launch {
                    uiEvent(CurveUiEvent.Update(curve.copy(index = it.text.toIntOrNull() ?: 0)))
                }
            },
            textStyle = TextStyle(
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    softKeyboard?.hide()
                    forceManager.clearFocus()
                }
            ),
            decorationBox = @Composable { innerTextField ->
                Column {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Numbers, contentDescription = null)
                        innerTextField()
                    }
                    Divider()
                }
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            modifier = Modifier.height(32.dp),
            checked = curve.enable,
            onCheckedChange = {
                scope.launch {
                    uiEvent(CurveUiEvent.Update(curve.copy(enable = it)))
                }
            }
        )
        ElevatedButton(onClick = {
            scope.launch {
                val points = curve.points.toMutableList()
                points.add(Point(0.0, 0.0))
                uiEvent(CurveUiEvent.Update(curve.copy(points = points)))
            }
        }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        }
    }
}