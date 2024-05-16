package com.zktony.android.ui.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf
import com.zktony.android.ui.navigation.NavigationActions

val LocalNavigationActions = staticCompositionLocalOf<NavigationActions> {
    error("No NavHostController provided")
}

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}