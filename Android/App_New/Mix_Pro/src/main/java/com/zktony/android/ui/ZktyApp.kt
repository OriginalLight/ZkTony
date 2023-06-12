package com.zktony.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
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

/**
 * Main entry point for the app.
 */
@Composable
fun ZktyApp() {

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
            AppNavHost(
                navController = navController,
                toggleDrawer = { navigationType.value = it }
            )
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
    NavHost(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.secondaryContainer
        ),
        navController = navController,
        startDestination = Route.SPLASH,
    ) {
        composable(Route.SPLASH) {
            ZktySplash(
                modifier = Modifier,
                navController = navController,
                toggleDrawer = toggleDrawer,
            )
        }
        composable(Route.HOME) {
            ZktyHome(
                modifier = Modifier,
                navController = navController,
                toggleDrawer = toggleDrawer,
            )
        }
        composable(Route.PROGRAM) {
            ZktyProgram(
                modifier = Modifier,
                navController = navController,
            )
        }
        composable(Route.CALIBRATION) {
            ZktyCalibration(
                modifier = Modifier,
                navController = navController,
            )
        }
        composable(Route.SETTING) {
            ZktySetting(
                modifier = Modifier,
                navController = navController,
            )
        }
        composable(Route.MOTOR) {
            ZktyMotor(
                modifier = Modifier,
                navController = navController,
            )
        }
        composable(Route.CONFIG) {
            ZktyConfig(
                modifier = Modifier,
                navController = navController,
            )
        }
    }
}