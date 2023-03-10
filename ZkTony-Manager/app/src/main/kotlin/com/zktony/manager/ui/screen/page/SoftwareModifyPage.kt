package com.zktony.manager.ui.screen.page

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.manager.R
import com.zktony.manager.data.remote.model.Software
import com.zktony.manager.ui.components.ManagerAppBar

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
    softwareChange: (Software) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler {
        onBack()
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

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
                value = software.`package`,
                onValueChange = { softwareChange(software.copy(`package` = it)) },
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
                value = software.version_name,
                onValueChange = { softwareChange(software.copy(version_name = it)) },
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
                value = software.version_code.toString(),
                onValueChange = {
                    softwareChange(
                        software.copy(
                            version_code = it.toIntOrNull() ?: 1
                        )
                    )
                },
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
                value = software.build_type,
                onValueChange = { softwareChange(software.copy(build_type = it)) },
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
                value = software.remarks,
                onValueChange = { softwareChange(software.copy(remarks = it)) },
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
            Row {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 16.dp), onClick = {
                        softwareChange(
                            Software().copy(
                                `package` = "",
                                version_name = "",
                                version_code = 1,
                                build_type = "",
                                remarks = ""
                            )
                        )
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                    )
                ) {
                    Text(text = "清空")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp), onClick = {
                    onBack()
                }) {
                    Text(text = "完成")
                }
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
        software = Software().copy(
            `package` = "com.example.www",
            version_name = "1.0.0",
            version_code = 1,
            build_type = "debug",
            remarks = "备注说明"
        ),
        softwareChange = {},
        onBack = {}
    )
}