package com.zktony.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DonutSmall
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.zktony.android.R

object Route {
    const val LOGIN = "Login"
    const val EXPERIMENTAL = "Experimental"
    const val PROGRAM = "Program"
    const val HISTORY = "History"
    const val SETTINGS = "Settings"
    const val SETTINGS_ARGUMENTS = "SettingsArguments"
    const val SETTINGS_DEBUG = "SettingsDebug"
}

data class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val iconTextId: Int
)

class NavigationActions(private val navController: NavHostController) {

    fun navController() = navController

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
        if (navController.previousBackStackEntry == null || navController.previousBackStackEntry?.destination?.route == Route.LOGIN) {
            // If we are already at the start destination, navigate to the experimental screen
            navigate(Route.EXPERIMENTAL)
        } else {
            navController.navigateUp()
        }
    }

    fun popBackStack() {
        navController.popBackStack()
    }

    @Composable
    fun selectDestination() = navController.currentBackStackEntryAsState().value?.destination?.route ?: Route.LOGIN
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