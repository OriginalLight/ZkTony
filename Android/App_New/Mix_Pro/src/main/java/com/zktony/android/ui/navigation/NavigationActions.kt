package com.zktony.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.zktony.android.R

object Route {
    const val HOME = "Home"
    const val PROGRAM = "Program"
    const val CONTAINER = "Container"
    const val CALIBRATION = "Calibration"
    const val SETTING = "Setting"
    const val MOTOR = "Motor"
    const val CONFIG = "Config"
    const val SPLASH = "Splash"
}

data class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val iconTextId: Int
)

class NavigationActions(private val navController: NavHostController) {

    fun navigateTo(destination: TopLevelDestination) {
        navController.navigate(destination.route) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }
}

val TOP_LEVEL_DESTINATIONS = listOf(
    TopLevelDestination(
        route = Route.HOME,
        icon = Icons.Default.Home,
        iconTextId = R.string.tab_home
    ),
    TopLevelDestination(
        route = Route.PROGRAM,
        icon = Icons.Default.Code,
        iconTextId = R.string.tab_program
    ),
    TopLevelDestination(
        route = Route.CONTAINER,
        icon = Icons.Default.Dashboard,
        iconTextId = R.string.tab_container
    ),
    TopLevelDestination(
        route = Route.CALIBRATION,
        icon = Icons.Default.Balance,
        iconTextId = R.string.tab_calibration
    ),
    TopLevelDestination(
        route = Route.SETTING,
        icon = Icons.Default.Settings,
        iconTextId = R.string.tab_setting
    )
)