package com.zktony.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.zktony.android.R

object Route {
    const val HOME = "Home"
    const val PROGRAM = "Program"
    const val EXPERIMENTRECORDS = "Experiment_records"
    const val CALIBRATION = "Calibration"
    const val SETTING = "Setting"
    const val SPLASH = "Splash"
}

data class TopLevelDestination(
    val id: Int,
    val route: String,
    val text: String,
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
        if (navController.previousBackStackEntry == null) {
            navigate(Route.HOME)
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
        id = 0,
        route = Route.HOME,
        text = "制胶操作",
        icon = Icons.Outlined.History,
        iconTextId = R.string.home
    ),
    TopLevelDestination(
        id = 10,
        route = Route.PROGRAM,
        text = "制胶程序",
        icon = Icons.Outlined.History,
        iconTextId = R.string.program
    ),
    TopLevelDestination(
        id = 11,
        route = Route.EXPERIMENTRECORDS,
        text = "实验记录",
        icon = Icons.Outlined.History,
        iconTextId = R.string.experiment_records
    ),
//    TopLevelDestination(
//        route = Route.CALIBRATION,
//        text = "校准管理",
//        iconTextId = R.string.calibration
//    ),
    TopLevelDestination(
        id = 5,
        route = Route.SETTING,
        text = "系统设置",
        icon = Icons.Outlined.History,
        iconTextId = R.string.setting
    ),
    TopLevelDestination(
        id = 4,
        route = Route.SPLASH,
        text = "首页",
        icon = Icons.Default.Home,
        iconTextId = R.string.splash
    )
)