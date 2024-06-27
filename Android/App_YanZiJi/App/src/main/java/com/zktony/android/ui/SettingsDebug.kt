package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.SettingsDebugViewModel

@Composable
fun SettingsDebugView(viewModel: SettingsDebugViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigate(Route.SETTINGS)
    }
}