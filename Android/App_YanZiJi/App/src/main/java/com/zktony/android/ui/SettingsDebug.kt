package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions

@Composable
fun SettingsDebugView() {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigate(Route.SETTINGS)
    }
}