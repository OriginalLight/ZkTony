package com.zktony.manager.ui.screen.page

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.proto.Customer
import com.zktony.proto.customer
import com.zktony.www.common.extension.currentTime
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 13:15
 */

// region CustomerModifyPage
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CustomerModifyPage(
    modifier: Modifier = Modifier,
    customer: Customer,
    onSave: (Customer) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler {
        onBack()
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val mName = remember { mutableStateOf(customer.name) }
    val mPhone = remember { mutableStateOf(customer.phone) }
    val mAddress = remember { mutableStateOf(customer.address) }
    val mIndustry = remember { mutableStateOf(customer.industry) }
    val mSource = remember { mutableStateOf(customer.source) }
    val mRemarks = remember { mutableStateOf(customer.remarks) }

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
            Spacer(modifier = Modifier.height(16.dp))
            Button(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
                onClick = {
                    onBack()
                    onSave(customer {
                        id = customer.id
                        name = mName.value
                        phone = mPhone.value
                        address = mAddress.value
                        industry = mIndustry.value
                        source = mSource.value
                        remarks = mRemarks.value
                        createTime = currentTime()
                    })
                }) {
                Text(text = "添加/修改")
            }
        }
    }
}
// endregion

// region Preview

@Preview
@Composable
fun CustomerModifyPagePreview() {
    CustomerModifyPage(
        customer = customer { id = UUID.randomUUID().toString() },
        onSave = {},
        onBack = {},
    )
}