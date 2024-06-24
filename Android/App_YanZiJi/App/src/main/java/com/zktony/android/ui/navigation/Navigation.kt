package com.zktony.android.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.zktony.android.ui.CalibrationRoute
import com.zktony.android.ui.DebugRoute
import com.zktony.android.ui.HistoryRoute
import com.zktony.android.ui.HomeRoute
import com.zktony.android.ui.LoginView
import com.zktony.android.ui.ProgramRoute
import com.zktony.android.ui.SettingRoute
import com.zktony.android.ui.SettingsView

@Composable
fun AppNavigation(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { scaffoldPadding ->

        NavHost(
            navController = navController,
            startDestination = Route.SETTINGS,
            modifier = Modifier
                .padding(scaffoldPadding)
                .consumeWindowInsets(scaffoldPadding)
        ) {
            composable(Route.LOGIN) { LoginView() }
            composable(Route.HOME) { HomeRoute(viewModel = hiltViewModel()) }
            composable(Route.PROGRAM) { ProgramRoute(viewModel = hiltViewModel()) }
            composable(Route.CALIBRATION) { CalibrationRoute(viewModel = hiltViewModel()) }
            composable(Route.HISTORY) { HistoryRoute(viewModel = hiltViewModel()) }
            composable(Route.DEBUG) { DebugRoute(viewModel = hiltViewModel()) }
            composable(Route.SETTING) { SettingRoute(viewModel = hiltViewModel()) }
            composable(Route.SETTINGS) { SettingsView() }
        }
    }
}