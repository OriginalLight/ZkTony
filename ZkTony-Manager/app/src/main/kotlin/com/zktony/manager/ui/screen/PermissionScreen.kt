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

    if (start) {
        content()
    } else {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Permissions needed", fontFamily = FontFamily.Serif) }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "You have to grant access to these permissions in order to use the app"
                )
                ListItem(
                    headlineText = { Text("Storage access") },
                    supportingText = { Text("Add photos from library when creating a log") },
                    trailingContent = { PermissionAccessIcon(state.value.hasStorageAccess) },
                    leadingContent = {
                        Icon(
                            Icons.Filled.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surfaceTint
                        )
                    }
                )
                Divider()
                ListItem(
                    headlineText = { Text("Camera access") },
                    supportingText = { Text("Take picture when creating a log") },
                    trailingContent = { PermissionAccessIcon(state.value.hasCameraAccess) },
                    leadingContent = {
                        Icon(
                            Icons.Filled.Camera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surfaceTint
                        )
                    }
                )
                Divider()
                ListItem(
                    headlineText = { Text("Precise location access") },
                    supportingText = { Text("Keep track of the location of a log") },
                    trailingContent = { PermissionAccessIcon(state.value.hasLocationAccess) },
                    leadingContent = {
                        Icon(
                            Icons.Filled.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surfaceTint
                        )
                    }
                )
                Spacer(Modifier.height(32.dp))
                if (state.value.hasAllAccess) {
                    FilledTonalButton(onClick = { start = true }) {
                        Text("Get started")
                    }
                } else {
                    if (hasRequestedPermissions) {
                        FilledTonalButton(onClick = { openSettings() }) {
                            Text("Go to settings")
                        }
                    } else {
                        FilledTonalButton(onClick = { requestPermissions.launch(REQUIRED_PERMISSIONS) }) {
                            Text("Request permissions")
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
            Icons.Filled.Check,
            contentDescription = "Permission accepted"
        )
    } else {
        Icon(
            Icons.Filled.Close,
            contentDescription = "Permission not granted"
        )
    }
}