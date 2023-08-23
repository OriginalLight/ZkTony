package com.zktony.android.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedDestination = navBackStackEntry?.destination?.route ?: Route.HOME
    val snackbarHostState = remember { SnackbarHostState() }
    val homeViewModel: HomeViewModel = koinViewModel()

    AppNavHost(
        modifier = Modifier,
        navController = navController,
        homeViewModel = homeViewModel,
        selectedDestination = selectedDestination,
        navigationActions = navigationActions,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    selectedDestination: String,
    navigationActions: NavigationActions,
    snackbarHostState: SnackbarHostState,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.SPLASH
    ) {
        composable(Route.SPLASH) {
            Splash(
                modifier = Modifier,
                navController = navController
            )
        }
        composable(Route.HOME) {
            HomeRoute(
                modifier = Modifier,
                navController = navController,
                viewModel = homeViewModel,
                selectedDestination = selectedDestination,
                navigationActions = navigationActions,
                snackbarHostState = snackbarHostState,
            )
        }
        composable(Route.PROGRAM) {
            Program(
                modifier = Modifier,
                navController = navController
            )
        }
        composable(Route.CALIBRATION) {
            Calibration(
                modifier = Modifier,
                navController = navController
            )
        }
        composable(Route.SETTING) {
            Setting(
                modifier = Modifier,
                navController = navController
            )
        }
    }
}