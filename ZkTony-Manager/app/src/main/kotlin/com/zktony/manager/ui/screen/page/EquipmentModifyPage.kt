package com.zktony.manager.ui.screen.page

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.manager.R
import com.zktony.manager.data.remote.model.Equipment
import com.zktony.manager.ui.components.ManagerAppBar

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 13:15
 */

// region EquipmentModifyPage
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EquipmentModifyPage(
    modifier: Modifier = Modifier,
    equipment: Equipment,
    isAdd: Boolean,
    onDone: (Equipment) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler {
        onBack()
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var temp by remember { mutableStateOf(equipment) }

    Column {
        ManagerAppBar(title = stringResource(id = R.string.page_customer_title),
            isFullScreen = true,
            onBack = { onBack() })

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxSize(),
        ) {
            val lazyColumnState = rememberLazyListState()
            LazyColumn(
                state = lazyColumnState,
                content = {
                    item {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            label = { Text(text = "设备名称") },
                            value = temp.name,
                            onValueChange = { temp = temp.copy(name = it) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(onNext = {
                                localFocusManager.moveFocus(FocusDirection.Next)
                            }),
                            maxLines = 1,
                            singleLine = true,
                        )
                    }
                    item {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            label = { Text(text = "设备型号") },
                            value = temp.model,
                            onValueChange = { temp = temp.copy(model = it) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = {
                                localFocusManager.moveFocus(FocusDirection.Next)
                            }),
                            maxLines = 1,
                            singleLine = true,
                        )
                    }
                    item {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            label = { Text(text = "设备电压") },
                            value = temp.voltage,
                            onValueChange = { temp = temp.copy(voltage = it) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = {
                                localFocusManager.moveFocus(FocusDirection.Next)
                            }),
                            maxLines = 1,
                            singleLine = true,
                        )
                    }
                    item {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            label = { Text(text = "设备功率") },
                            value = temp.power,
                            onValueChange = { temp = temp.copy(power = it) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = {
                                localFocusManager.moveFocus(FocusDirection.Next)
                            }),
                            maxLines = 1,
                            singleLine = true,
                        )
                    }
                    item {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            label = { Text(text = "设备频率") },
                            value = temp.frequency,
                            onValueChange = { temp = temp.copy(frequency = it) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = {
                                localFocusManager.moveFocus(FocusDirection.Next)
                            }),
                            maxLines = 1,
                            singleLine = true,
                        )
                    }
                    item {
                        var attachment by remember { mutableStateOf("") }
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            label = { Text(text = "设备附件") },
                            value = attachment,
                            onValueChange = { attachment = it },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (attachment.isNotEmpty()) {
                                            temp =
                                                temp.copy(attachment = temp.attachment + attachment + " ")
                                            attachment = ""
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add",
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = {
                                localFocusManager.moveFocus(FocusDirection.Next)
                            }),
                            maxLines = 1,
                            singleLine = true,
                        )
                        AnimatedVisibility(visible = temp.attachment.isNotEmpty()) {
                            Column {
                                val list = temp.attachment.split(" ").map { it.trim() }
                                list.forEachIndexed { index, it ->
                                    if (it.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                modifier = Modifier
                                                    .padding(start = 16.dp),
                                                text = (index + 1).toString(),
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Text(
                                                modifier = Modifier
                                                    .padding(start = 16.dp),
                                                text = it,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            IconButton(
                                                modifier = Modifier
                                                    .padding(end = 16.dp),
                                                onClick = {
                                                    temp = temp.copy(
                                                        attachment = temp.attachment.replace(
                                                            "$it ",
                                                            ""
                                                        )
                                                    )
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            label = { Text(text = "备注说明") },
                            value = temp.remarks,
                            onValueChange = { temp = temp.copy(remarks = it) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                                onBack()
                                onDone(temp)
                            }),
                            maxLines = 10,
                            singleLine = false,
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                            onClick = {
                                onBack()
                                onDone(temp)
                            }) {
                            Text(text = if (isAdd) "添加" else "修改")
                        }
                    }
                }
            )
        }
    }
}
// endregion

// region Preview

@Preview
@Composable
fun EquipmentModifyPagePreview() {
    EquipmentModifyPage(
        equipment = Equipment(),
        isAdd = true,
        onDone = {},
        onBack = {},
    )
}