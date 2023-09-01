package com.zktony.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.zktony.android.R

object Route {
    const val Home = "Home"
    const val Program = "Program"
    const val Curve = "Curve"
    const val History = "History"
    const val Setting = "Setting"
    const val Splash = "Splash"
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

    fun navigateUp() {
        navController.navigateUp()
    }
}

val TOP_LEVEL_DESTINATIONS = listOf(
    TopLevelDestination(
        route = Route.Program,
        icon = Icons.Outlined.Terminal,
        iconTextId = R.string.program
    ),
    TopLevelDestination(
        route = Route.Curve,
        icon = Icons.Outlined.Analytics,
        iconTextId = R.string.calibration
    ),
    TopLevelDestination(
        route = Route.History,
        icon = Icons.Outlined.History,
        iconTextId = R.string.history
    ),
    TopLevelDestination(
        route = Route.Setting,
        icon = Icons.Outlined.Settings,
        iconTextId = R.string.settings
    )
)