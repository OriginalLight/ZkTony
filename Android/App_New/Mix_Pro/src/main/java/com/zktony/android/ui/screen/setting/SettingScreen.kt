package com.zktony.android.ui.screen.setting

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.ui.components.ZkTonyTopAppBar
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
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler {
        if (uiState.page == PageEnum.MAIN) {
            navController.navigateUp()
        } else {
            viewModel.navigationTo(PageEnum.MAIN)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            AnimatedVisibility(visible = uiState.page == PageEnum.AUTHENTICATION) {
                ZkTonyTopAppBar(
                    title = stringResource(id = R.string.authentication),
                    navigation = {
                        if (uiState.page == PageEnum.MAIN) {
                            navController.navigateUp()
                        } else {
                            viewModel.navigationTo(PageEnum.MAIN)
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AnimatedVisibility(visible = uiState.page == PageEnum.MAIN) {
                        SettingMainPage(
                            modifier = modifier,
                            uiState = uiState,
                            checkUpdate = viewModel::checkUpdate,
                            navigationTo = viewModel::navigationTo,
                            openWifi = viewModel::openWifi,
                            setLanguage = viewModel::setLanguage,
                            setNavigation = viewModel::setNavigation,
                        )
                    }

                    AnimatedVisibility(visible = uiState.page == PageEnum.AUTHENTICATION) {
                        AuthenticationPage(
                            modifier = modifier,
                            navController = navController,
                            navigationTo = viewModel::navigationTo,
                        )
                    }
                }
            }
        }
    )
}