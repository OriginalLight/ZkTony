package com.zktony.android.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.zktony.android.ui.CalibrationRoute
import com.zktony.android.ui.DebugRoute
import com.zktony.android.ui.HistoryRoute
import com.zktony.android.ui.HomeRoute
import com.zktony.android.ui.HomeViewModel
import com.zktony.android.ui.ProgramRoute
import com.zktony.android.ui.SettingRoute
import com.zktony.android.ui.Splash
import com.zktony.android.utils.SnackbarUtils

@Composable
fun AppNavigation(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    snackbarHostState: SnackbarHostState
) {

    val message by SnackbarUtils.message.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = message) {
        // Handle snackbar messages
        message?.let {
            snackbarHostState.showSnackbar(it)
            SnackbarUtils.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { scaffoldPadding ->

        NavHost(
            navController = navController,
            startDestination = Route.SPLASH,
            modifier = Modifier
                .padding(scaffoldPadding)
                .consumeWindowInsets(scaffoldPadding)
        ) {
            composable(Route.SPLASH) { Splash() }
            composable(Route.HOME) { HomeRoute(viewModel = homeViewModel) }
            composable(Route.PROGRAM) { ProgramRoute(viewModel = hiltViewModel()) }
            composable(Route.CALIBRATION) { CalibrationRoute(viewModel = hiltViewModel()) }
            composable(Route.HISTORY) { HistoryRoute(viewModel = hiltViewModel()) }
            composable(Route.DEBUG) { DebugRoute(viewModel = hiltViewModel()) }
            composable(Route.SETTING) { SettingRoute(viewModel = hiltViewModel()) }
        }
    }
}