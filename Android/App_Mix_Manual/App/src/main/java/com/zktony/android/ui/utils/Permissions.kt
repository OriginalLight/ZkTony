package com.zktony.android.ui.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

@Composable
fun LocationPermission() {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 权限被授予，继续你的操作
        } else {
            // 权限被拒绝，处理拒绝逻辑
        }
    }

    val hasPermission = ContextCompat.checkSelfPermission(
        LocalContext.current,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasPermission) {
        // 请求权限
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}


@Composable
fun PermissionsScreen() {
    val context = LocalContext.current
    val permissionsGranted = remember { mutableStateOf(false) }

    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.entries.all { it.value }
        permissionsGranted.value = allPermissionsGranted
    }

    LaunchedEffect(Unit) {
        val permissionsNeeded = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionsNeeded.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsNeeded.toTypedArray())
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Permissions(
    content: @Composable () -> Unit
) {
    val permissionStates = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    )

    if (permissionStates.allPermissionsGranted) {
        // All permissions are granted
        content()
    } else {
        Column {
            // Not all permissions are granted
            // Show the permissions screen
            // Request permissions
            permissionStates.permissions.forEach {
                when (it.permission) {
                    Manifest.permission.READ_EXTERNAL_STORAGE -> {
                        when {
                            it.status.isGranted -> {
                                /* Permission has been granted by the user.
                                   You can use this permission to now acquire the location of the device.
                                   You can perform some other tasks here.
                                */
                                Text(text = "Read Ext Storage permission has been granted")
                            }

                            it.status.shouldShowRationale -> {
                                /*Happens if a user denies the permission two times

                                 */
                                Text(text = "Read Ext Storage permission is needed")
                            }

                            !it.status.isGranted && !it.status.shouldShowRationale -> {
                                /* If the permission is denied and the should not show rationale
                                    You can only allow the permission manually through app settings
                                 */
                                Text(text = "Navigate to settings and enable the Storage permission")

                            }
                        }
                    }

                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                        // Request write external storage permission
                        // Request permissions
                        when {
                            it.status.isGranted -> {
                                /* Permission has been granted by the user.
                                   You can use this permission to now acquire the location of the device.
                                   You can perform some other tasks here.
                                */
                                Text(text = "Write Ext Storage permission has been granted")
                            }

                            it.status.shouldShowRationale -> {
                                /*Happens if a user denies the permission two times

                                 */
                                Text(text = "Write Ext Storage permission is needed")
                            }

                            !it.status.isGranted && !it.status.shouldShowRationale -> {
                                /* If the permission is denied and the should not show rationale
                                    You can only allow the permission manually through app settings
                                 */
                                Text(text = "Navigate to settings and enable the Storage permission")

                            }
                        }
                    }
                }
            }

            Button(onClick = { permissionStates.launchMultiplePermissionRequest() }) {
                Text(text = "Request Permissions")
            }
        }
    }
}

@Preview
@Composable
fun PermissionsPreview() {
    Permissions(content = {})
}
