package com.zktony.manager.ui.fragment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.zktony.manager.ui.viewmodel.CustomerViewModel
import com.zktony.manager.ui.viewmodel.ManagerPageEnum
import com.zktony.proto.customer
import com.zktony.www.common.extension.currentTime
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 13:15
 */

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CustomerEditFragment(
    modifier: Modifier = Modifier,
    navigateTo: (ManagerPageEnum) -> Unit,
    viewModel: CustomerViewModel,
    isDualPane: Boolean = false

) {
    BackHandler {
        navigateTo(ManagerPageEnum.CUSTOMER_LIST)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val customer = uiState.customer ?: customer {}

    val mName = remember { mutableStateOf(customer.name) }
    val mPhone = remember { mutableStateOf(customer.phone) }
    val mAddress = remember { mutableStateOf(customer.address) }
    val mIndustry = remember { mutableStateOf(customer.industry) }
    val mSource = remember { mutableStateOf(customer.source) }
    val mRemarks = remember { mutableStateOf(customer.remarks) }

    Column(
        modifier = modifier
    ) {
        ManagerAppBar(
            title = "客户信息",
            isFullScreen = !isDualPane,
            onBack = { navigateTo(ManagerPageEnum.CUSTOMER_LIST) })

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
                    label = { Text(text = "客户姓名") },
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
                    label = { Text(text = "客户电话") },
                    value = mPhone.value,
                    onValueChange = { mPhone.value = it },
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
                    label = { Text(text = "客户地址") },
                    value = mAddress.value,
                    onValueChange = { mAddress.value = it },
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
                    label = { Text(text = "信息来源") },
                    value = mSource.value,
                    onValueChange = { mSource.value = it },
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
                    label = { Text(text = "客户行业") },
                    value = mIndustry.value,
                    onValueChange = { mIndustry.value = it },
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
                if (uiState.customer == null) {
                    Button(modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                        enabled = mName.value.isNotEmpty() && mPhone.value.isNotEmpty(),
                        onClick = {
                            navigateTo(ManagerPageEnum.CUSTOMER_LIST)
                            viewModel.insert(customer {
                                id = UUID.randomUUID().toString()
                                name = mName.value
                                phone = mPhone.value
                                address = mAddress.value
                                industry = mIndustry.value
                                source = mSource.value
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
                            navigateTo(ManagerPageEnum.CUSTOMER_LIST)
                            viewModel.update(customer {
                                id = customer.id
                                name = mName.value
                                phone = mPhone.value
                                address = mAddress.value
                                industry = mIndustry.value
                                source = mSource.value
                                remarks = mRemarks.value
                            })
                        }) {
                        Text(text = "修改")
                    }

                    if (customer.id.isNotEmpty()) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            onClick = {
                                viewModel.delete(customer.id)
                                navigateTo(ManagerPageEnum.CUSTOMER_LIST)
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