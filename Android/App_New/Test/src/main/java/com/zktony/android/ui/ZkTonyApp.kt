package com.zktony.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.zktony.android.ui.screen.HomeScreen
import com.zktony.android.ui.screen.LcScreen
import com.zktony.android.ui.utils.NavigationType
import kotlinx.coroutines.launch
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
    val navigationType = remember { mutableStateOf(NavigationType.PERMANENT_NAVIGATION_DRAWER) }
    val scope = rememberCoroutineScope()

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
                ) {
                    scope.launch {
                        navigationType.value = NavigationType.NAVIGATION_RAIL
                    }
                }
            }
            AnimatedVisibility(
                visible = navigationType.value == NavigationType.NAVIGATION_RAIL,
                enter = expandHorizontally(),
                exit = shrinkHorizontally(),
            ) {
                AppNavigationRail(
                    selectedDestination = selectedDestination,
                    navigateToTopLevelDestination = navigationActions::navigateTo
                ) {
                    scope.launch {
                        navigationType.value = NavigationType.PERMANENT_NAVIGATION_DRAWER
                    }
                }
            }
        }) {
        AppNavHost(
            modifier = Modifier,
            navController = navController,
        )
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
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.HOME,
    ) {
        composable(Route.HOME) {
            HomeScreen(
                modifier = Modifier,
                navController = navController,
                viewModel = koinViewModel(),
            )
        }
        composable(Route.LC) {
            LcScreen(
                modifier = Modifier,
                navController = navController,
                viewModel = koinViewModel(),
            )
        }
    }
}
