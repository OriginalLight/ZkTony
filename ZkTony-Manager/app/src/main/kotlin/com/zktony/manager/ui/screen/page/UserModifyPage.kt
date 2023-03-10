package com.zktony.manager.ui.screen.page

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.manager.data.local.model.User
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.manager.ui.screen.viewmodel.SettingPage
import com.zktony.manager.ui.screen.viewmodel.SettingUiState

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 13:22
 */


// region: UserModifyPage
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun UserModifyPage(
    uiState: SettingUiState,
    navigateTo: (SettingPage) -> Unit,
    onUserChange: (User) -> Unit,
    onSaveUser: () -> Unit,
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current

    BackHandler {
        navigateTo(SettingPage.SETTING)
    }

    ManagerAppBar(
        title = "个人信息",
        isFullScreen = true,
        onBack = { navigateTo(SettingPage.SETTING) },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        OutlinedTextField(
            value = uiState.user.name,
            onValueChange = { onUserChange(uiState.user.copy(name = it)) },
            modifier = Modifier
                .fillMaxWidth(),
            label = { Text(text = "姓名") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                )
            },
            trailingIcon = {
                AnimatedVisibility(uiState.user.name.isNotEmpty()) {
                    IconButton(onClick = {
                        onUserChange(uiState.user.copy(name = ""))
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = null,
                        )
                    }
                }
            },
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(onNext = {
                localFocusManager.moveFocus(focusDirection = FocusDirection.Down)
            }),
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = uiState.user.phone,
            onValueChange = { onUserChange(uiState.user.copy(phone = it)) },
            modifier = Modifier
                .fillMaxWidth(),
            label = { Text(text = "手机号") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                )
            },
            trailingIcon = {
                AnimatedVisibility(uiState.user.phone.isNotEmpty()) {
                    IconButton(onClick = {
                        onUserChange(uiState.user.copy(phone = ""))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                        )
                    }
                }
            },
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Phone
            ),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
                navigateTo(SettingPage.SETTING)
            }),
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = {
                navigateTo(SettingPage.SETTING)
            },
        ) {
            Text(text = "完成")
        }
    }
}
// endregion

// region Preview
@Preview
@Composable
fun UserModifyPagePreview() {
    UserModifyPage(
        uiState = SettingUiState(),
        navigateTo = {},
        onUserChange = {},
        onSaveUser = {},
    )
}
// endregion