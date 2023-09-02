package com.zktony.android.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zktony.android.data.datastore.LocalDataSaver
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.navigation.Route
import com.zktony.android.utils.extra.dataSaver
import org.koin.androidx.compose.koinViewModel


val LocalNavigationActions = staticCompositionLocalOf<NavigationActions> {
    error("No NavHostController provided")
}

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

@Composable
fun App() {

    val navController = rememberNavController()
    val navigationActions = remember(navController) { NavigationActions(navController) }
    val snackbarHostState = remember { SnackbarHostState() }
    val homeViewModel: HomeViewModel = koinViewModel()

    CompositionLocalProvider(
        LocalNavigationActions provides navigationActions,
        LocalSnackbarHostState provides snackbarHostState,
        LocalDataSaver provides dataSaver
    ) {
        AppNavigation(
            navController = navController,
            homeViewModel = homeViewModel,
            snackbarHostState = snackbarHostState
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { scaffoldPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Splash,
            modifier = Modifier
                .padding(scaffoldPadding)
                .consumeWindowInsets(scaffoldPadding)
        ) {
            composable(Route.Splash) { Splash() }
            composable(Route.Home) { HomeRoute(viewModel = homeViewModel) }
            composable(Route.Program) { ProgramRoute(viewModel = koinViewModel()) }
            composable(Route.Curve) { CurveRoute(viewModel = koinViewModel()) }
            composable(Route.History) { HistoryRoute(viewModel = koinViewModel()) }
            composable(Route.Setting) { SettingRoute(viewModel = koinViewModel()) }
        }
    }
}