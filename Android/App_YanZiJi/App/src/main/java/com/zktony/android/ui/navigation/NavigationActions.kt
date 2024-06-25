package com.zktony.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DonutSmall
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.zktony.android.R

object Route {
    const val LOGIN = "Login"
    const val EXPERIMENTAL = "Experimental"
    const val PROGRAM = "Program"
    const val HISTORY = "History"
    const val SETTINGS = "Settings"
}

data class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val iconTextId: Int
)

class NavigationActions(private val navController: NavHostController) {
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
        if (navController.previousBackStackEntry == null) {
            navigate(Route.EXPERIMENTAL)
        } else {
            navController.navigateUp()
        }
    }

    fun popBackStack() {
        navController.popBackStack()
    }
}

val TOP_LEVEL_DESTINATIONS = listOf(
    TopLevelDestination(
        route = Route.EXPERIMENTAL,
        icon = Icons.Outlined.DonutSmall,
        iconTextId = R.string.program
    ),
    TopLevelDestination(
        route = Route.PROGRAM,
        icon = Icons.Outlined.Terminal,
        iconTextId = R.string.program
    ),
    TopLevelDestination(
        route = Route.HISTORY,
        icon = Icons.Outlined.History,
        iconTextId = R.string.history
    ),
    TopLevelDestination(
        route = Route.SETTINGS,
        icon = Icons.Outlined.Settings,
        iconTextId = R.string.settings
    )
)