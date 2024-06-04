package com.zktony.android.ui.navigation

import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import com.zktony.android.ui.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { scaffoldPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.SPLASH,
            modifier = Modifier
                .padding(scaffoldPadding)
                .consumeWindowInsets(scaffoldPadding)
        ) {
            //制胶程序
            composable(Route.HOME) { HomeRoute(viewModel = homeViewModel) }
            //制胶操作
            composable(Route.PROGRAM) { ProgramRoute(viewModel = hiltViewModel()) }
//            //校准设置
//            composable(Route.CALIBRATION) { CalibrationRoute(viewModel = hiltViewModel()) }
            //实验记录
            composable(Route.EXPERIMENTRECORDS) {ExperimentRecords(viewModel = hiltViewModel())}
            //系统设置
            composable(Route.SETTING) { SettingRoute(viewModel = hiltViewModel()) }
            //首页
            composable(Route.SPLASH) { Splash(viewModel = homeViewModel) }
        }
    }
}