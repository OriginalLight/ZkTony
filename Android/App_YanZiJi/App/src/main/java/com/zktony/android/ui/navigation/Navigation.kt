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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.zktony.android.ui.ExperimentalView
import com.zktony.android.ui.HistoryView
import com.zktony.android.ui.LoginView
import com.zktony.android.ui.ProgramView
import com.zktony.android.ui.SettingsAgingView
import com.zktony.android.ui.SettingsArgumentsEquipmentView
import com.zktony.android.ui.SettingsArgumentsPumpView
import com.zktony.android.ui.SettingsArgumentsRuntimeView
import com.zktony.android.ui.SettingsArgumentsSensorView
import com.zktony.android.ui.SettingsArgumentsView
import com.zktony.android.ui.SettingsArgumentsVoltageView
import com.zktony.android.ui.SettingsDebugView
import com.zktony.android.ui.SettingsFqcView
import com.zktony.android.ui.SettingsView
import com.zktony.android.ui.components.BottomBar
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState

@Composable
fun AppNavigation() {

    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current
    val selectedDestination = navigationActions.selectDestination()

    Row {
        AnimatedVisibilityWithLogin(selectedDestination) {
            AppNavigationDrawer(
                selectedDestination = selectedDestination,
                navigationActions = navigationActions
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
                navController = navigationActions.navController(),
                startDestination = Route.LOGIN,
            ) {
                composable(Route.LOGIN) { LoginView() }
                composable(Route.PROGRAM) { ProgramView() }
                composable(Route.EXPERIMENTAL) { ExperimentalView() }
                composable(Route.HISTORY) { HistoryView() }
                composable(Route.SETTINGS) { SettingsView() }
                composable(Route.SETTINGS_ARGUMENTS) { SettingsArgumentsView() }
                composable(Route.SETTINGS_ARGUMENTS_EQUIPMENT) { SettingsArgumentsEquipmentView() }
                composable(Route.SETTINGS_ARGUMENTS_RUNTIME) { SettingsArgumentsRuntimeView() }
                composable(Route.SETTINGS_ARGUMENTS_PUMP) { SettingsArgumentsPumpView() }
                composable(Route.SETTINGS_ARGUMENTS_VOLTAGE) { SettingsArgumentsVoltageView() }
                composable(Route.SETTINGS_ARGUMENTS_SENSOR) { SettingsArgumentsSensorView() }
                composable(Route.SETTINGS_DEBUG) { SettingsDebugView() }
                composable(Route.SETTINGS_FQC) { SettingsFqcView() }
                composable(Route.SETTINGS_AGING) { SettingsAgingView() }

            }
        }
    }
}

@Composable
fun AppNavigationDrawer(
    modifier: Modifier = Modifier,
    selectedDestination: String,
    navigationActions: NavigationActions
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
                    selected = selectedDestination.contains(destination.route),
                    onClick = { navigationActions.navigate(destination.route) },
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