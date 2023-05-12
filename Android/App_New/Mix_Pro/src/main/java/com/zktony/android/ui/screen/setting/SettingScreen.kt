package com.zktony.android.ui.screen.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

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

    AnimatedVisibility(visible = uiState.page == SettingPage.SETTING) {
        SettingPage(
            modifier = modifier,
            checkUpdate = viewModel::checkUpdate,
            navController = navController,
            navigationTo = viewModel::navigateTo,
            openWifi = viewModel::openWifi,
            setLanguage = viewModel::setLanguage,
            setNavigation = viewModel::setNavigation,
            uiState = uiState,
        )
    }

    AnimatedVisibility(visible = uiState.page == SettingPage.AUTHENTICATION) {
        AuthenticationPage(
            modifier = modifier,
            navController = navController,
            navigationTo = viewModel::navigateTo,
        )
    }
}