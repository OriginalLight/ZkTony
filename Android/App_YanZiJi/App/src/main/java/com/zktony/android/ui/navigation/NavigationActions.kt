package com.zktony.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.zktony.android.R
import com.zktony.android.ui.icons.Experimental
import com.zktony.android.ui.icons.Log
import com.zktony.android.ui.icons.Program
import com.zktony.android.ui.icons.Settings

object Route {
    const val LOGIN = "Login"
    const val EXPERIMENTAL = "Experimental"
    const val PROGRAM = "Program"
    const val PROGRAM_ADD_OR_UPDATE = "ProgramAddOrUpdate"
    const val LOG = "Log"
    const val LOG_DETAIL = "LogDetail"
    const val SETTINGS = "Settings"
    const val SETTINGS_ACCESSORY_LIFE = "SettingsAccessoryLife"
    const val SETTINGS_MAINTENANCE_RECORD = "SettingsMaintenanceRecord"
    const val SETTINGS_ERROR_LOG = "SettingsErrorLog"
    const val SETTINGS_VERSION_INFO = "SettingsVersionInfo"
    const val SETTINGS_USER_MANAGEMENT = "SettingsUserManagement"
    const val SETTINGS_ARGUMENTS = "SettingsArguments"
    const val SETTINGS_ARGUMENTS_EQUIPMENT = "SettingsArgumentsEquipment"
    const val SETTINGS_ARGUMENTS_RUNTIME = "SettingsArgumentsRuntime"
    const val SETTINGS_ARGUMENTS_PUMP = "SettingsArgumentsPump"
    const val SETTINGS_ARGUMENTS_VOLTAGE = "SettingsArgumentsVoltage"
    const val SETTINGS_ARGUMENTS_SENSOR = "SettingsArgumentsSensor"
    const val SETTINGS_DEBUG = "SettingsDebug"
    const val SETTINGS_DEBUG_SOLENOID_VALVE = "SettingsDebugSolenoidValve"
    const val SETTINGS_DEBUG_PIPELINE = "SettingsDebugPipeline"
    const val SETTINGS_DEBUG_EXPERIMENTAL = "SettingsDebugExperimental"
    const val SETTINGS_FQC = "SettingsFqc"
    const val SETTINGS_AGING = "SettingsAging"
    const val SETTINGS_AGING_MODULE = "SettingsAgingModule"
    const val SETTINGS_AGING_COMPLETE = "SettingsAgingComplete"
    const val SETTINGS_RUNTIME_LOG = "SettingsRuntimeLog"
}

// Navigation actions
class NavigationActions(private val navController: NavHostController) {

    // 导航控制器
    fun navController() = navController

    // 导航到指定页面
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

    // 导航到上一层
    fun navigateUp() {
        if (navController.previousBackStackEntry == null) {
            // If we are already at the start destination, navigate to the experimental screen
            navigate(Route.EXPERIMENTAL)
        } else {
            navController.navigateUp()
        }
    }

    // 清空上一个返回栈
    fun popBackStack() {
        navController.popBackStack()
    }

    // 当前选中的页面
    @Composable
    fun selectDestination() =
        navController.currentBackStackEntryAsState().value?.destination?.route ?: Route.LOGIN
}

// 顶级目的地
data class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val iconTextId: Int
)

// 顶级目的地列表
val TOP_LEVEL_DESTINATIONS = listOf(
    TopLevelDestination(
        route = Route.EXPERIMENTAL,
        icon = Experimental,
        iconTextId = R.string.app_experimental
    ),
    TopLevelDestination(
        route = Route.PROGRAM,
        icon = Program,
        iconTextId = R.string.app_program
    ),
    TopLevelDestination(
        route = Route.LOG,
        icon = Log,
        iconTextId = R.string.app_log
    ),
    TopLevelDestination(
        route = Route.SETTINGS,
        icon = Settings,
        iconTextId = R.string.app_settings
    )
)