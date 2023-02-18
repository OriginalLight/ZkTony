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

import android.Manifest.permission.*
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    content: @Composable () -> Unit,
) {
    val requestPermission = mutableListOf(
        CAMERA,
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        requestPermission.add(READ_MEDIA_IMAGES)
    } else {
        requestPermission.add(READ_EXTERNAL_STORAGE)
    }

    val allPermission = rememberMultiplePermissionsState(permissions = requestPermission)

    var storageAccess by remember { mutableStateOf(false) }
    var cameraAccess by remember { mutableStateOf(false) }
    var fineAccess by remember { mutableStateOf(false) }
    var coarseAccess by remember { mutableStateOf(false) }

    allPermission.permissions.forEach {
        when (it.permission) {
            CAMERA -> cameraAccess = it.status.isGranted
            ACCESS_FINE_LOCATION -> fineAccess = it.status.isGranted
            ACCESS_COARSE_LOCATION -> coarseAccess = it.status.isGranted
            READ_MEDIA_IMAGES -> storageAccess = it.status.isGranted
            READ_EXTERNAL_STORAGE -> storageAccess = it.status.isGranted
        }
    }

    AnimatedVisibility(visible = allPermission.allPermissionsGranted) {
        content()
    }
    AnimatedVisibility(visible = !allPermission.allPermissionsGranted) {
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
                    trailingContent = { PermissionAccessIcon(storageAccess) },
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
                    trailingContent = { PermissionAccessIcon(cameraAccess) },
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
                    trailingContent = { PermissionAccessIcon(fineAccess && coarseAccess) },
                    leadingContent = {
                        Icon(
                            Icons.Filled.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surfaceTint
                        )
                    })
                Spacer(Modifier.height(32.dp))
                FilledTonalButton(onClick = { allPermission.launchMultiplePermissionRequest() }) {
                    Text("请求权限")
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
