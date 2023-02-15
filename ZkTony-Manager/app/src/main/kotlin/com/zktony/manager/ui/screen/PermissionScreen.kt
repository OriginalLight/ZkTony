/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zktony.manager.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zktony.manager.ui.utils.PermissionManager.Companion.REQUIRED_PERMISSIONS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
    content: @Composable () -> Unit,
    viewModel: PermissionViewModel = viewModel(factory = PermissionViewModelFactory())
) {

    val state = viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hasRequestedPermissions by remember { mutableStateOf(false) }

    val requestPermissions =
        rememberLauncherForActivityResult(RequestMultiplePermissions()) { permissions ->
            hasRequestedPermissions = true
            viewModel.onPermissionChange(permissions)
        }

    fun openSettings() {
        ContextCompat.startActivity(context, viewModel.createSettingsIntent(), null)
    }

    var start by remember { mutableStateOf(false) }

    AnimatedVisibility(visible = start) {
        content()
    }
    AnimatedVisibility(visible = !start) {
        Scaffold(topBar = {
            TopAppBar(title = { Text("所需权限", fontFamily = FontFamily.Cursive) })
        }) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(16.dp), text = "您必须授予对这些权限的访问权限"
                )
                ListItem(headlineText = { Text("存储访问") },
                    supportingText = { Text("创建业务时从图库添加照片") },
                    trailingContent = { PermissionAccessIcon(state.value.hasStorageAccess) },
                    leadingContent = {
                        Icon(
                            Icons.Filled.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surfaceTint
                        )
                    })
                Divider()
                ListItem(headlineText = { Text("摄像头访问") },
                    supportingText = { Text("扫描二维码时拍照") },
                    trailingContent = { PermissionAccessIcon(state.value.hasCameraAccess) },
                    leadingContent = {
                        Icon(
                            Icons.Filled.Camera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surfaceTint
                        )
                    })
                Divider()
                ListItem(headlineText = { Text("精确的位置访问") },
                    supportingText = { Text("跟踪业务的位置") },
                    trailingContent = { PermissionAccessIcon(state.value.hasLocationAccess) },
                    leadingContent = {
                        Icon(
                            Icons.Filled.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surfaceTint
                        )
                    })
                Spacer(Modifier.height(32.dp))
                if (state.value.hasAllAccess) {
                    FilledTonalButton(onClick = { start = true }) {
                        Text("START")
                    }
                } else {
                    if (hasRequestedPermissions) {
                        FilledTonalButton(onClick = { openSettings() }) {
                            Text("打开设置")
                        }
                    } else {
                        FilledTonalButton(onClick = { requestPermissions.launch(REQUIRED_PERMISSIONS) }) {
                            Text("请求权限")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionAccessIcon(hasAccess: Boolean) {
    if (hasAccess) {
        Icon(
            Icons.Filled.Check, contentDescription = "接受权限"
        )
    } else {
        Icon(
            Icons.Filled.Close, contentDescription = "未授予权限"
        )
    }
}
