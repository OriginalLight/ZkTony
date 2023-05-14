package com.zktony.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
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
import com.zktony.android.ui.screen.EmptyComingSoon
import com.zktony.android.ui.screen.calibration.CalibrationScreen
import com.zktony.android.ui.screen.config.ConfigScreen
import com.zktony.android.ui.screen.container.ContainerScreen
import com.zktony.android.ui.screen.home.HomeScreen
import com.zktony.android.ui.screen.motor.MotorScreen
import com.zktony.android.ui.screen.setting.SettingScreen
import org.koin.androidx.compose.koinViewModel

/**
 * Main entry point for the app.
 */
@Composable
fun ZkTonyApp() {

    val navController = rememberNavController()
    val navigationActions = remember(navController) {
        NavigationActions(navController)
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedDestination = navBackStackEntry?.destination?.route ?: Route.HOME

    val drawerState = remember { mutableStateOf(false) }

    PermanentNavigationDrawer(drawerContent = {
        AnimatedVisibility(
            visible = !drawerState.value,
            enter = expandHorizontally(),
            exit = shrinkHorizontally(),
        ) {
            PermanentNavigationDrawerContent(
                selectedDestination = selectedDestination,
                navigateToTopLevelDestination = navigationActions::navigateTo,
            ) {
                drawerState.value = true
            }
        }
        AnimatedVisibility(
            visible = drawerState.value,
            enter = expandHorizontally(),
            exit = shrinkHorizontally(),
        ) {
            AppNavigationRail(
                selectedDestination = selectedDestination,
                navigateToTopLevelDestination = navigationActions::navigateTo
            ) {
                drawerState.value = false
            }
        }
    }) {
        AppNavHost(navController = navController)
    }
}

/**
 * NavHost for the app
 *
 * @param navController NavHostController
 * @param modifier Modifier
 */
@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.HOME,
    ) {
        composable(Route.HOME) {
            HomeScreen(
                modifier = Modifier,
                viewModel = koinViewModel(),
            )
        }
        composable(Route.PROGRAM) {
            EmptyComingSoon()
        }
        composable(Route.CONTAINER) {
            ContainerScreen(
                modifier = Modifier,
                navController = navController,
                viewModel = koinViewModel(),
            )
        }
        composable(Route.CALIBRATION) {
            CalibrationScreen(
                modifier = Modifier,
                navController = navController,
                viewModel = koinViewModel(),
            )
        }
        composable(Route.Setting) {
            SettingScreen(
                modifier = Modifier,
                navController = navController,
                viewModel = koinViewModel(),
            )
        }
        composable(Route.MOTOR) {
            MotorScreen(
                modifier = Modifier,
                navController = navController,
                viewModel = koinViewModel(),
            )
        }
        composable(Route.CONFIG) {
            ConfigScreen(
                modifier = Modifier,
                navController = navController,
                viewModel = koinViewModel(),
            )
        }
    }
}
