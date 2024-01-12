package com.zktony.manager.ui.fragment

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.manager.ui.viewmodel.InstrumentViewModel
import com.zktony.manager.ui.viewmodel.ManagerPageEnum
import com.zktony.proto.instrument
import com.zktony.www.common.extension.currentTime
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 13:15
 */

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InstrumentEditFragment(
    modifier: Modifier = Modifier,
    navigateTo: (ManagerPageEnum) -> Unit,
    viewModel: InstrumentViewModel,
    isDualPane: Boolean = false
) {
    BackHandler {
        navigateTo(ManagerPageEnum.INSTRUMENT_LIST)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val instrument = uiState.instrument ?: instrument {}

    val mName = remember { mutableStateOf(instrument.name) }
    val mModel = remember { mutableStateOf(instrument.model) }
    val mVoltage = remember { mutableStateOf(instrument.voltage) }
    val mPower = remember { mutableStateOf(instrument.power) }
    val mFrequency = remember { mutableStateOf(instrument.frequency) }
    val mAttachment = remember { mutableStateOf(instrument.attachment) }
    val mRemarks = remember { mutableStateOf(instrument.remarks) }

    Column(
        modifier = modifier
    ) {
        ManagerAppBar(
            title = "仪器信息",
            isFullScreen = !isDualPane,
            onBack = { navigateTo(ManagerPageEnum.INSTRUMENT_LIST) })

        Spacer(modifier = Modifier.height(16.dp))

        val lazyColumnState = rememberLazyListState()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            state = lazyColumnState,
        ) {
            item {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    label = { Text(text = "设备名称") },
                    value = mName.value,
                    onValueChange = { mName.value = it },
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
                    value = mModel.value,
                    onValueChange = { mModel.value = it },
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
                    value = mVoltage.value,
                    onValueChange = { mVoltage.value = it },
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
                    value = mPower.value,
                    onValueChange = { mPower.value = it },
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
                    value = mFrequency.value,
                    onValueChange = { mFrequency.value = it },
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
                                    mAttachment.value = mAttachment.value + attachment + "|"
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
                AnimatedVisibility(visible = mAttachment.value.isNotEmpty()) {
                    Column {
                        val list = mAttachment.value.split("|").map { it.trim() }
                        var index = 1
                        list.forEach {
                            if (it.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .padding(start = 16.dp),
                                        text = index.toString(),
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
                                            mAttachment.value = mAttachment.value.replace(
                                                "$it|",
                                                ""
                                            )
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                        )
                                    }
                                }
                                Divider(thickness = 1.dp, color = Color.LightGray)
                                index++
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
                    value = mRemarks.value,
                    onValueChange = { mRemarks.value = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                    }),
                    maxLines = 10,
                    singleLine = false,
                )
            }
            item {
                if (uiState.instrument == null) {
                    Button(modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                        enabled = mName.value.isNotEmpty() && mModel.value.isNotEmpty(),
                        onClick = {
                            navigateTo(ManagerPageEnum.INSTRUMENT_LIST)
                            viewModel.insert(instrument {
                                id = UUID.randomUUID().toString()
                                name = mName.value
                                model = mModel.value
                                voltage = mVoltage.value
                                power = mPower.value
                                frequency = mFrequency.value
                                attachment = mAttachment.value
                                remarks = mRemarks.value
                                createTime = currentTime()
                            })
                        }) {
                        Text(text = "添加")
                    }
                } else {
                    Button(modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                        onClick = {
                            navigateTo(ManagerPageEnum.INSTRUMENT_LIST)
                            viewModel.update(instrument {
                                id = instrument.id
                                name = mName.value
                                model = mModel.value
                                voltage = mVoltage.value
                                power = mPower.value
                                frequency = mFrequency.value
                                attachment = mAttachment.value
                                remarks = mRemarks.value
                            })
                        }) {
                        Text(text = "修改")
                    }
                    if (instrument.id.isNotEmpty()) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            onClick = {
                                navigateTo(ManagerPageEnum.INSTRUMENT_LIST)
                                viewModel.delete(instrument.id)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                            )
                        ) {
                            Text(text = "删除")
                        }
                    }
                }
            }
        }
    }
}