package com.zktony.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zktony.android.R
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(
    modifier: Modifier = Modifier,
    dateMillis: Long,
    onDateChange: (Long) -> Unit,
    onDismissRequest: () -> Unit,
) {
    // 上面的代码是一个AlertDialog组件，用于选择日期和时间。
    // 第一步先选择日期，然后选择时间。
    // 最上方加一个步骤指示器，显示当前步骤。


    var step by remember { mutableIntStateOf(0) }
    var selectedMillis by remember { mutableLongStateOf(dateMillis) }
    val hour = Calendar.getInstance().apply { timeInMillis = dateMillis }.get(Calendar.HOUR_OF_DAY)
    val minute = Calendar.getInstance().apply { timeInMillis = dateMillis }.get(Calendar.MINUTE)
    val timePickerState =
        rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
    val datePackerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)

    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.small
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (step) {
                0 -> {
                    DatePicker(state = datePackerState)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        // 取消按钮
                        // 下一步按钮
                        OutlinedButton(modifier = Modifier.padding(end = 16.dp),
                            onClick = { onDismissRequest() }) {
                            Text(
                                text = stringResource(id = R.string.cancel),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Button(onClick = {
                            selectedMillis = datePackerState.selectedDateMillis ?: selectedMillis
                            // 下一步
                            step = 1
                        }) {
                            Text(
                                text = stringResource(id = R.string.next),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                1 -> {
                    // 上一步按钮
                    IconButton(onClick = { step = 0 }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.Reply,
                            contentDescription = "ArrowBack"
                        )
                    }
                    // 选择时间
                    // 选择时间的代码
                    // 从dateMillis中提取小时和分钟
                    // 时间选择器
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        // 取消按钮
                        // 上一步按钮
                        // 确认按钮
                        OutlinedButton(modifier = Modifier.padding(end = 16.dp),
                            onClick = { onDismissRequest() }) {
                            Text(
                                text = stringResource(id = R.string.cancel),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Button(onClick = {
                            // 更新时间
                            selectedMillis = Calendar.getInstance().apply {
                                timeInMillis = selectedMillis
                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                set(Calendar.MINUTE, timePickerState.minute)
                            }.timeInMillis
                            onDateChange(selectedMillis)
                        }) {
                            Text(
                                text = stringResource(id = R.string.ok),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}