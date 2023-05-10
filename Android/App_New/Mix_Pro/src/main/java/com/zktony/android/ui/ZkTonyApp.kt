package com.zktony.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.zktony.android.ui.screen.*
import com.zktony.android.ui.viewmodel.*
import org.koin.androidx.compose.koinViewModel

/**
 * Main entry point for the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
            visible = !drawerState.value, enter = expandHorizontally(), exit = shrinkHorizontally()
        ) {
            PermanentNavigationDrawerContent(
                selectedDestination = selectedDestination,
                navigateToTopLevelDestination = navigationActions::navigateTo,
            ) {
                drawerState.value = true
            }
        }
        AnimatedVisibility(
            visible = drawerState.value, enter = expandHorizontally(), exit = shrinkHorizontally()
        ) {
            AppNavigationRail(
                selectedDestination = selectedDestination,
                navigateToTopLevelDestination = navigationActions::navigateTo
            ) {
                drawerState.value = false
            }
        }
    }) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.inverseOnSurface)
            ) {
                AppNavHost(
                    navController = navController,
                    modifier = Modifier.weight(1f),
                )
            }
        }
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
            EmptyComingSoon()
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
