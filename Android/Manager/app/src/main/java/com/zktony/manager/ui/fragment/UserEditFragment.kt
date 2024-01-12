package com.zktony.manager.ui.fragment

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.manager.data.local.entity.User
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.manager.ui.viewmodel.SettingPage
import com.zktony.manager.ui.viewmodel.UserViewModel

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 13:22
 */


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun UserEditFragment(
    modifier: Modifier = Modifier,
    navigateTo: (SettingPage) -> Unit,
    viewModel: UserViewModel,
    isDualPane: Boolean = false
) {
    BackHandler {
        navigateTo(SettingPage.SETTING)
    }


    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val userName = remember { mutableStateOf(uiState.user.name) }
    val userPhone = remember { mutableStateOf(uiState.user.phone) }


    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current


    ManagerAppBar(
        title = "个人信息",
        isFullScreen = !isDualPane,
        onBack = { navigateTo(SettingPage.SETTING) },
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        OutlinedTextField(
            value = userName.value,
            onValueChange = { userName.value = it },
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
                AnimatedVisibility(userName.value.isNotEmpty()) {
                    IconButton(onClick = {
                        userName.value = ""
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
            value = userPhone.value,
            onValueChange = { userPhone.value = it },
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
                AnimatedVisibility(userPhone.value.isNotEmpty()) {
                    IconButton(onClick = {
                        userPhone.value = ""
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
                viewModel.insert(
                    User(
                        id = uiState.user.id,
                        name = userName.value,
                        phone = userPhone.value
                    )
                )
                navigateTo(SettingPage.SETTING)
            },
        ) {
            Text(text = "完成")
        }
    }
}
