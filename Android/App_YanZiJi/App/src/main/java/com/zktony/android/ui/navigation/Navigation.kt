package com.zktony.android.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.zktony.android.ui.SettingsDebugExperimentalView
import com.zktony.android.ui.SettingsDebugPipelineView
import com.zktony.android.ui.SettingsDebugSolenoidValveView
import com.zktony.android.ui.SettingsDebugView
import com.zktony.android.ui.SettingsFqcView
import com.zktony.android.ui.SettingsRuntimeLogView
import com.zktony.android.ui.SettingsUserManagementView
import com.zktony.android.ui.SettingsView
import com.zktony.android.ui.components.BottomBar
import com.zktony.android.ui.components.LogoutConfirmDialog
import com.zktony.android.ui.icons.Logout
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.zktyBrush
import com.zktony.android.utils.AuthUtils
import com.zktony.android.utils.SnackbarUtils
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {

    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current
    val selectedDestination = navigationActions.selectDestination()
    val snackbar by SnackbarUtils.snackbar.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = snackbar) {
        snackbar?.let {
            snackbarHostState.showSnackbar(it)
            SnackbarUtils.clearSnackbar()
        }
    }

    Row(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .padding(if (selectedDestination == Route.LOGIN) 0.dp else 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibilityWithLogin(selectedDestination) {
            AppNavigationDrawer(
                selectedDestination = selectedDestination,
                navigationActions = navigationActions
            )
        }
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = { AnimatedVisibilityWithLogin(selectedDestination) { BottomBar() } },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                composable(Route.SETTINGS_USER_MANAGEMENT) { SettingsUserManagementView() }
                composable(Route.SETTINGS_ARGUMENTS) { SettingsArgumentsView() }
                composable(Route.SETTINGS_ARGUMENTS_EQUIPMENT) { SettingsArgumentsEquipmentView() }
                composable(Route.SETTINGS_ARGUMENTS_RUNTIME) { SettingsArgumentsRuntimeView() }
                composable(Route.SETTINGS_ARGUMENTS_PUMP) { SettingsArgumentsPumpView() }
                composable(Route.SETTINGS_ARGUMENTS_VOLTAGE) { SettingsArgumentsVoltageView() }
                composable(Route.SETTINGS_ARGUMENTS_SENSOR) { SettingsArgumentsSensorView() }
                composable(Route.SETTINGS_DEBUG) { SettingsDebugView() }
                composable(Route.SETTINGS_DEBUG_SOLENOID_VALVE) { SettingsDebugSolenoidValveView() }
                composable(Route.SETTINGS_DEBUG_PIPELINE) { SettingsDebugPipelineView() }
                composable(Route.SETTINGS_DEBUG_EXPERIMENTAL) { SettingsDebugExperimentalView() }
                composable(Route.SETTINGS_FQC) { SettingsFqcView() }
                composable(Route.SETTINGS_AGING) { SettingsAgingView() }
                composable(Route.SETTINGS_RUNTIME_LOG) { SettingsRuntimeLogView() }
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
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        LogoutConfirmDialog(onDismiss = { showDialog = false }) {
            scope.launch {
                showDialog = false
                AuthUtils.logout()
                navigationActions.navigate(Route.LOGIN)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(120.dp)
            .background(
                shape = MaterialTheme.shapes.medium,
                brush = zktyBrush
            )
            .padding(vertical = 16.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TOP_LEVEL_DESTINATIONS.forEach { destination ->
                NavigationItem(
                    selected = selectedDestination.startsWith(destination.route),
                    onClick = { navigationActions.navigate(destination.route) },
                    icon = destination.icon,
                    text = stringResource(id = destination.iconTextId)
                )
            }
        }

        // Logout
        Box(
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.BottomCenter)
                .clip(MaterialTheme.shapes.medium)
                .clickable { showDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = Logout,
                contentDescription = "Logout",
                tint = Color.White
            )
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

@Composable
fun NavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    colors: NavigationRailItemColors = NavigationRailItemDefaults.colors(),
) {
    val backgroundColor = if (selected) colors.selectedIndicatorColor else Color.Transparent
    val contentColor = if (selected) colors.selectedTextColor else Color.White

    Box(
        modifier = Modifier
            .background(color = backgroundColor, shape = MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = icon,
                contentDescription = text,
                tint = contentColor
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
        }
    }
}