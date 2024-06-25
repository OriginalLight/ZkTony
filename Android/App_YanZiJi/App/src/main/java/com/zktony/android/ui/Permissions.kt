package com.zktony.android.ui

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
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
    val progressions = listOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    val permissionStates = rememberMultiplePermissionsState(permissions = progressions)

    if (permissionStates.allPermissionsGranted) {
        // All permissions are granted
        content()
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {


            Text(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                text = "请授予应用程序所需的权限",
                style = MaterialTheme.typography.displaySmall
            )

            Column(
                modifier = Modifier.fillMaxWidth(0.5f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Not all permissions are granted
                // Show the permissions screen
                // Request permissions
                permissionStates.permissions.forEach {
                    when (it.permission) {
                        Manifest.permission.READ_EXTERNAL_STORAGE -> {
                            PermissionView(state = it, text = "读取外部存储")
                        }

                        Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                            PermissionView(state = it, text = "写入外部存储")
                        }
                    }
                }

                Button(
                    onClick = { permissionStates.launchMultiplePermissionRequest() }
                ) {
                    Text(text = "请求权限", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionView(
    state: PermissionState,
    text: String
) {
    val icon = when {
        state.status.isGranted -> "✅"
        state.status.shouldShowRationale -> "⚠️"
        else -> "❌"
    }

    ListItem(
        modifier = Modifier.clip(MaterialTheme.shapes.small),
        headlineContent = { Text(text = text) },
        trailingContent = { Text(text = icon) },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}


@Preview
@Composable
fun PermissionsPreview() {
    Permissions(content = {})
}