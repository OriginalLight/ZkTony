package com.zktony.android.ui.components

import androidx.compose.runtime.Composable
import com.zktony.android.data.Role
import com.zktony.android.utils.AuthUtils

@Composable
fun RequirePermission(
    role: Role,
    content: @Composable () -> Unit
) {
    if (role.permission >= AuthUtils.getRole().permission) {
        content()
    }
}