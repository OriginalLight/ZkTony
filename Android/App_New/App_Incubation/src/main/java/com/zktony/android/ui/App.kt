package com.zktony.android.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.navigation.Route
import org.koin.androidx.compose.koinViewModel

/**
 * Main entry point for the app.
 */
@Composable
fun App() {

    val navController = rememberNavController()
    val navigationActions = remember(navController) {
        NavigationActions(navController)
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val homeViewModel: HomeViewModel = koinViewModel()

    AppNavHost(
        modifier = Modifier,
        navController = navController,
        homeViewModel = homeViewModel,
        navigationActions = navigationActions,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    navigationActions: NavigationActions,
    snackbarHostState: SnackbarHostState,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.Splash
    ) {
        composable(Route.Splash) {
            Splash(
                modifier = Modifier,
                navController = navController
            )
        }
        composable(Route.Home) {
            HomeRoute(
                navController = navController,
                viewModel = homeViewModel,
                navigationActions = navigationActions,
                snackbarHostState = snackbarHostState,
            )
        }
        composable(Route.Program) {
            ProgramRoute(
                navController = navController,
                viewModel = koinViewModel(),
                snackbarHostState = snackbarHostState
            )
        }
        composable(Route.Curve) {
            CurveRoute(
                navController = navController,
                viewModel = koinViewModel(),
                snackbarHostState = snackbarHostState
            )
        }
        composable(Route.History) {
            HistoryRoute(
                navController = navController,
                viewModel = koinViewModel(),
                snackbarHostState = snackbarHostState
            )
        }
        composable(Route.Setting) {
            SettingRoute(
                navController = navController,
                viewModel = koinViewModel(),
                snackbarHostState = snackbarHostState
            )
        }
    }
}