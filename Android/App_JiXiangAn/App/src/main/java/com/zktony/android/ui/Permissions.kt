package com.zktony.android.ui

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * A composable that displays the permissions screen.
 * This screen is shown when the app needs to request permissions from the user.
 * The user can grant or deny permissions from this screen.
 * If the user denies permissions, the app will not be able to function properly.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Permissions(
    content: @Composable () -> Unit
) {
    val permissionStates = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
                when(it.permission) {
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
                            !it.status.isGranted  && !it.status.shouldShowRationale -> {
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