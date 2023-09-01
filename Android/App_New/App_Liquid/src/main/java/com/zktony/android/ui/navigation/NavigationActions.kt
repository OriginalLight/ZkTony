package com.zktony.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.zktony.android.R

object Route {
    const val Home = "Home"
    const val Program = "Program"
    const val Calibration = "Calibration"
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
        iconTextId = R.string.tab_program
    ),
    TopLevelDestination(
        route = Route.Calibration,
        icon = Icons.Outlined.Analytics,
        iconTextId = R.string.tab_calibration
    ),
    TopLevelDestination(
        route = Route.Setting,
        icon = Icons.Outlined.Settings,
        iconTextId = R.string.tab_setting
    )
)