package com.zktony.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zktony.android.ui.navigation.AppNavigationRail
import com.zktony.android.ui.navigation.ModalNavigationDrawerContent
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.NavigationContentPosition
import com.zktony.android.ui.utils.NavigationType
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Main entry point for the app.
 */
@Composable
fun App() {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navController = rememberNavController()
    val navigationActions = remember(navController) {
        NavigationActions(navController)
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedDestination = navBackStackEntry?.destination?.route ?: Route.HOME
    val navigationType = remember { mutableStateOf(NavigationType.NONE) }

    Surface(color = MaterialTheme.colorScheme.surface) {

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = false,
            drawerContent = {
                ModalNavigationDrawerContent(
                    selectedDestination = selectedDestination,
                    navigationContentPosition = NavigationContentPosition.CENTER,
                    navigateToTopLevelDestination = navigationActions::navigateTo,
                    onDrawerClicked = { scope.launch { drawerState.close() } },
                )
            },
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = navigationType.value == NavigationType.NAVIGATION_RAIL,
                    enter = expandHorizontally(),
                    exit = shrinkHorizontally(),
                ) {
                    AppNavigationRail(
                        selectedDestination = selectedDestination,
                        navigationContentPosition = NavigationContentPosition.CENTER,
                        navigateToTopLevelDestination = navigationActions::navigateTo,
                        onDrawerClicked = { scope.launch { drawerState.open() } },
                    )
                }
                AppNavHost(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    toggleDrawer = { navigationType.value = it },
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