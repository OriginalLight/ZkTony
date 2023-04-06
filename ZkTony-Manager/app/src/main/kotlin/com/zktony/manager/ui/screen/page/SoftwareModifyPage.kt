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
import com.zktony.proto.Software
import com.zktony.proto.software
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 13:15
 */

// region SoftwareModifyPage
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SoftwareModifyPage(
    modifier: Modifier = Modifier,
    software: Software,
    onBack: () -> Unit,
    onSave: (Software) -> Unit,
) {
    BackHandler {
        onBack()
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    
    val mPackage = remember { mutableStateOf(software.`package`) }
    val mVersionName = remember { mutableStateOf(software.versionName) }
    val mVersionCode = remember { mutableStateOf(software.versionCode) }
    val mBuildType = remember { mutableStateOf(software.buildType) }
    val mRemarks = remember { mutableStateOf(software.remarks) }



    Column {
        ManagerAppBar(title = stringResource(id = R.string.page_software_title),
            isFullScreen = true,
            onBack = { onBack() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxSize(),
        ) {
            // 修改光标位置在文字后
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text(text = "包名 : com.example.www") },
                value = mPackage.value,
                onValueChange = { mPackage.value = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
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
                label = { Text(text = "版本名 : xx.xx.xx") },
                value = mVersionName.value,
                onValueChange = { mVersionName.value = it },
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
                label = { Text(text = "版本号 : 1、2、3...") },
                value = mVersionCode.value.toString(),
                onValueChange = { mVersionCode.value = it.toIntOrNull() ?: 1 },
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
                label = { Text(text = "构建类型 : debug/release") },
                value = mBuildType.value,
                onValueChange = { mBuildType.value = it },
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
                    onBack()
                }),
                maxLines = 10,
                singleLine = false,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                onClick = {
                    onSave(
                        software {
                            id = software.id
                            package_ = mPackage.value
                            versionName = mVersionName.value
                            versionCode = mVersionCode.value
                            buildType = mBuildType.value
                            remarks = mRemarks.value
                        }
                    )
                }
            ) {
                Text(text = "保存")
            }
        }
    }
}
// endregion

// region Preview

@Preview
@Composable
fun SoftwareModifyPagePreview() {
    SoftwareModifyPage(
        software = software { id = UUID.randomUUID().toString() },
        onBack = {},
        onSave = {}
    )
}