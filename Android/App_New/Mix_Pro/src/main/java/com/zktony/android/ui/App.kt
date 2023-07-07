package com.zktony.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zktony.android.ui.navigation.AppNavigationRail
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.navigation.PermanentNavigationDrawerContent
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.NavigationType
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
    val navigationType = remember { mutableStateOf(NavigationType.NONE) }

    PermanentNavigationDrawer(
        drawerContent = {
            AnimatedVisibility(
                visible = navigationType.value == NavigationType.PERMANENT_NAVIGATION_DRAWER,
                enter = expandHorizontally(),
                exit = shrinkHorizontally(),
            ) {
                PermanentNavigationDrawerContent(
                    selectedDestination = selectedDestination,
                    navigateToTopLevelDestination = navigationActions::navigateTo,
                    onDrawerClicked = { navigationType.value = NavigationType.NAVIGATION_RAIL },
                )
            }
            AnimatedVisibility(
                visible = navigationType.value == NavigationType.NAVIGATION_RAIL,
                enter = expandHorizontally(),
                exit = shrinkHorizontally(),
            ) {
                AppNavigationRail(
                    selectedDestination = selectedDestination,
                    navigateToTopLevelDestination = navigationActions::navigateTo,
                    onDrawerClicked = {
                        navigationType.value = NavigationType.PERMANENT_NAVIGATION_DRAWER
                    },
                )
            }
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.outlineVariant)
            ) {
                AppNavHost(
                    navController = navController,
                    toggleDrawer = { navigationType.value = it }
                )
            }

        }
    )
}

/**
 * NavHost for the app
 *
 * @param navController NavHostController
 * @param modifier Modifier
 */
@Composable
private fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    toggleDrawer: (NavigationType) -> Unit = {},
) {
    val homeViewModel: HomeViewModel = koinViewModel()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.SPLASH,
    ) {
        composable(Route.SPLASH) {
            Splash(
                modifier = Modifier,
                navController = navController,
                toggleDrawer = toggleDrawer,
            )
        }
        composable(Route.HOME) {
            Home(
                modifier = Modifier,
                navController = navController,
                toggleDrawer = toggleDrawer,
                viewModel = homeViewModel,
            )
        }
        composable(Route.PROGRAM) {
            Program(
                modifier = Modifier,
                navController = navController,
            )
        }
        composable(Route.CALIBRATION) {
            Calibration(
                modifier = Modifier,
                navController = navController,
            )
        }
        composable(Route.SETTING) {
            Setting(
                modifier = Modifier,
                navController = navController,
            )
        }
        composable(Route.MOTOR) {
            Motor(
                modifier = Modifier,
                navController = navController,
            )
        }
        composable(Route.CONFIG) {
            Config(
                modifier = Modifier,
                navController = navController,
            )
        }
    }
}