package com.zktony.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.zktony.android.R

object Route {
    const val HOME = "Home"
    const val PROGRAM = "Program"
    const val CALIBRATION = "Calibration"
    const val HISTORY = "History"
    const val DEBUG = "Debug"
    const val SETTING = "Setting"
    const val SPLASH = "Splash"
}

data class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val iconTextId: Int
)

class NavigationActions(private val navController: NavHostController) {
    fun navigateTo(destination: TopLevelDestination) = navigate(destination.route)

    fun navigate(route: String) {
        navController.navigate(route) {
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

    fun navigateUp() {
        navController.navigateUp()
    }

    fun popBackStack() {
        navController.popBackStack()
    }
}

val TOP_LEVEL_DESTINATIONS = listOf(
    TopLevelDestination(
        route = Route.PROGRAM,
        icon = Icons.Outlined.Terminal,
        iconTextId = R.string.program
    ),
    TopLevelDestination(
        route = Route.CALIBRATION,
        icon = Icons.Outlined.Analytics,
        iconTextId = R.string.calibration
    ),
    TopLevelDestination(
        route = Route.HISTORY,
        icon = Icons.Outlined.History,
        iconTextId = R.string.history
    ),
    TopLevelDestination(
        route = Route.DEBUG,
        icon = Icons.Outlined.Checklist,
        iconTextId = R.string.debug
    ),
    TopLevelDestination(
        route = Route.SETTING,
        icon = Icons.Outlined.Settings,
        iconTextId = R.string.setting
    )
)