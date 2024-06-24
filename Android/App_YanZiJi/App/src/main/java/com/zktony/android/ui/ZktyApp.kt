package com.zktony.android.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.ui.navigation.AppNavigation
import com.zktony.android.utils.SnackbarUtils

@Composable
fun ZktyApp(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    // 收集Snackbar消息
    val snackbar by SnackbarUtils.snackbar.collectAsStateWithLifecycle()
    // 处理Snackbar消息
    LaunchedEffect(key1 = snackbar) {
        snackbar?.let {
            snackbarHostState.showSnackbar(it)
            SnackbarUtils.clearSnackbar()
        }
    }

    Permissions {
        AppNavigation(
            navController = navController,
            snackbarHostState = snackbarHostState
        )
    }
}