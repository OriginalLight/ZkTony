package com.zktony.manager.ui.screen.page

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.zktony.manager.data.remote.model.Customer
import com.zktony.manager.ui.components.ManagerAppBar

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
    navigateTo: () -> Unit,
    isAdd: Boolean,
    onDone: (Customer) -> Unit,
) {
    BackHandler {
        navigateTo()
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var tempCustomer by remember { mutableStateOf(customer) }

    Column {
        ManagerAppBar(title = stringResource(id = R.string.page_customer_title),
            isFullScreen = true,
            onBack = { navigateTo() })

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
                value = tempCustomer.name,
                onValueChange = {  tempCustomer = tempCustomer.copy(name = it) },
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
                value = tempCustomer.phone,
                onValueChange = { tempCustomer = tempCustomer.copy(phone = it) },
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
                value = tempCustomer.address,
                onValueChange = { tempCustomer = tempCustomer.copy(address = it) },
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
                value = tempCustomer.source,
                onValueChange = { tempCustomer = tempCustomer.copy(source = it) },
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
                value = tempCustomer.industry,
                onValueChange = { tempCustomer = tempCustomer.copy(industry = it) },
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
                value = tempCustomer.remarks,
                onValueChange = { tempCustomer = tempCustomer.copy(remarks = it) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                    navigateTo()
                    onDone(tempCustomer)
                }),
                maxLines = 10,
                singleLine = false,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
                onClick = {
                    navigateTo()
                    onDone(tempCustomer)
                }) {
                Text(text = if (isAdd) "添加" else "修改")
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
        customer = Customer(),
        navigateTo = {},
        isAdd = true,
        onDone = {},
    )
}