package com.zktony.android.ui.screen.setting

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.ui.navigation.PageEnum

/**
 * Setting screen
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param viewModel SettingViewModel
 */
@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: SettingViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var page by remember { mutableStateOf(PageEnum.MAIN) }

    BackHandler {
        if (page == PageEnum.MAIN) {
            navController.navigateUp()
        } else {
            page = PageEnum.MAIN
        }
    }

    AnimatedVisibility(visible = page == PageEnum.MAIN) {
        SettingMainPage(
            modifier = modifier,
            checkUpdate = viewModel::checkUpdate,
            navController = navController,
            navigationTo = { page = it },
            openWifi = viewModel::openWifi,
            setLanguage = viewModel::setLanguage,
            setNavigation = viewModel::setNavigation,
            uiState = uiState,
        )
    }

    AnimatedVisibility(visible = page == PageEnum.AUTHENTICATION) {
        AuthenticationPage(
            modifier = modifier,
            navController = navController,
            navigationTo = { page = it },
        )
    }
}