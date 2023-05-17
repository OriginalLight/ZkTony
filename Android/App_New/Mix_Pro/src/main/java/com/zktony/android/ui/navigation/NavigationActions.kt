package com.zktony.android.ui.navigation

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
    val iconId: Int,
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
        iconId = R.drawable.ic_home,
        iconTextId = R.string.tab_home
    ),
    TopLevelDestination(
        route = Route.PROGRAM,
        iconId = R.drawable.ic_program,
        iconTextId = R.string.tab_program
    ),
    TopLevelDestination(
        route = Route.CONTAINER,
        iconId = R.drawable.ic_container,
        iconTextId = R.string.tab_container
    ),
    TopLevelDestination(
        route = Route.CALIBRATION,
        iconId = R.drawable.ic_calibration,
        iconTextId = R.string.tab_calibration
    ),
    TopLevelDestination(
        route = Route.SETTING,
        iconId = R.drawable.ic_admin,
        iconTextId = R.string.tab_setting
    )
)