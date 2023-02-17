/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zktony.manager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material.icons.outlined.Store
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.zktony.manager.R

object Route {
    const val HOME = "Home"
    const val STORE = "Store"
    const val MANAGER = "Manager"
    const val SETTING = "Setting"
}

data class TopLevelDestination(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
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
        selectedIcon = Icons.Default.LocalShipping,
        unselectedIcon = Icons.Outlined.LocalShipping,
        iconTextId = R.string.tab_home
    ),
    TopLevelDestination(
        route = Route.STORE,
        selectedIcon = Icons.Default.Store,
        unselectedIcon = Icons.Outlined.Store,
        iconTextId = R.string.tab_store
    ),
    TopLevelDestination(
        route = Route.MANAGER,
        selectedIcon = Icons.Default.Source,
        unselectedIcon = Icons.Outlined.Source,
        iconTextId = R.string.tab_manager
    ),
    TopLevelDestination(
        route = Route.SETTING,
        selectedIcon = Icons.Default.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        iconTextId = R.string.tab_setting
    )

)
