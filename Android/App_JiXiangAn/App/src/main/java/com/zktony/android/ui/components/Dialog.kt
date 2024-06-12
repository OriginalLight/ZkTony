package com.zktony.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zktony.android.R
import com.zktony.android.ui.HomeIntent
import com.zktony.android.utils.extra.timeFormat
import kotlinx.coroutines.delay

/**
 * @author 刘贺贺
 * @date 2023/6/2 13:21
 */

@Composable
fun InputDialog(
    title: String = "添加",
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    var value by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onCancel) {
        ElevatedCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = value,
                    onValueChange = { value = it },
                    textStyle = TextStyle(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    singleLine = true,
                    shape = CircleShape,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        onConfirm(value)
                    })
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        onClick = { onCancel() }
                    ) {
                        Text(text = stringResource(id = R.string.cancel))
                    }

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        onClick = { onConfirm(value) },
                        enabled = value.isNotBlank()
                    ) {
                        Text(text = stringResource(id = R.string.confirm))
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorDialog(
    message: String,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onConfirm) {
        ElevatedCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = { onConfirm() }
                ) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            }
        }
    }
}


@Composable
fun CleanDialog(
    job: Int,
    dispatch: (HomeIntent) -> Unit,
    onCancel: () -> Unit
) {

    var time by remember { mutableLongStateOf(30 * 60L) }

    LaunchedEffect(key1 = job) {
        while (job == 1) {
            time -= 1
            delay(1000L)
        }
    }

    Dialog(onDismissRequest = { }) {
        ElevatedCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "自动清理",
                    style = MaterialTheme.typography.titleLarge,
                )

                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text(text = "请取走孵育完成的膜")
                    Text(text = "请放回孵育盒")
                    Text(text = "请将抗体试剂瓶置空")
                    Text(text = "请确保洗涤液不低于500mL")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        onClick = {
                            if (job != 0) {
                                dispatch(HomeIntent.AutoClean)
                            }
                            onCancel()
                        }
                    ) {
                        Text(
                            text = if (job == 1) "中止" else "返回",
                            color = if (job == 1) MaterialTheme.colorScheme.error else Color.Black
                        )
                    }

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        onClick = {
                            if (job == 0) {
                                dispatch(HomeIntent.AutoClean)
                            }
                        }
                    ) {
                        Text(
                            text = when (job) {
                                0 -> {
                                    "开始"
                                }

                                1 -> {
                                    time.timeFormat()
                                }

                                2 -> {
                                    "已完成"
                                }

                                else -> {
                                    "Unknown"
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 二次确认对话框
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        ElevatedCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        onClick = { onCancel() }
                    ) {
                        Text(text = stringResource(id = R.string.cancel))
                    }

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        onClick = { onConfirm() }
                    ) {
                        Text(text = stringResource(id = R.string.confirm))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewInputDialog() {
    InputDialog(onConfirm = {}, onCancel = {})
}

@Preview
@Composable
fun PreviewErrorDialog() {
    ErrorDialog(message = "错误", onConfirm = {})
}

@Preview
@Composable
fun PreviewCleanDialog() {
    CleanDialog(job = 0, dispatch = {}) {

    }
}

@Preview
@Composable
fun PreviewConfirmDialog() {
    ConfirmDialog(
        title = "确认",
        message = "确认删除？",
        onConfirm = {},
        onCancel = {}
    )
}