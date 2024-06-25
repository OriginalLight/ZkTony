package com.zktony.android.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.zktony.android.ui.ExperimentalView
import com.zktony.android.ui.HistoryView
import com.zktony.android.ui.LoginView
import com.zktony.android.ui.ProgramView
import com.zktony.android.ui.SettingsView
import com.zktony.android.ui.components.BottomBar

@Composable
fun AppNavigation(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    val selectedDestination =
        navController.currentBackStackEntryAsState().value?.destination?.route ?: Route.LOGIN

    Row {
        AnimatedVisibilityWithLogin(selectedDestination) {
            AppNavigationDrawer(
                selectedDestination = selectedDestination,
                navController = navController
            )
        }
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = { AnimatedVisibilityWithLogin(selectedDestination) { BottomBar() } }
        ) { scaffoldPadding ->
            NavHost(
                modifier = Modifier
                    .padding(scaffoldPadding)
                    .consumeWindowInsets(scaffoldPadding),
                navController = navController,
                startDestination = Route.LOGIN,
            ) {
                composable(Route.LOGIN) { LoginView() }
                composable(Route.PROGRAM) { ProgramView(viewModel = hiltViewModel()) }
                composable(Route.EXPERIMENTAL) { ExperimentalView(viewModel = hiltViewModel()) }
                composable(Route.HISTORY) { HistoryView(viewModel = hiltViewModel()) }
                composable(Route.SETTINGS) { SettingsView() }
            }
        }
    }
}

@Composable
fun AppNavigationDrawer(
    modifier: Modifier = Modifier,
    selectedDestination: String,
    navController: NavHostController
) {
    NavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .width(100.dp),
        containerColor = MaterialTheme.colorScheme.inverseSurface
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TOP_LEVEL_DESTINATIONS.forEach { destination ->
                NavigationRailItem(
                    selected = selectedDestination == destination.route,
                    onClick = { navController.navigate(destination.route) },
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = stringResource(
                                id = destination.iconTextId
                            )
                        )
                    }
                )
            }
        }
    }

}

@Composable
fun AnimatedVisibilityWithLogin(selectedDestination: String, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = selectedDestination != Route.LOGIN,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        content()
    }
}