package com.zktony.android.ui.screen.calibration

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
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
import com.zktony.android.ui.components.ZkTonyBottomAddAppBar
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.navigation.PageEnum

/**
 * Calibration screen
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param viewModel CalibrationViewModel
 * @return Unit
 */
@Composable
fun CalibrationScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: CalibrationViewModel,
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
            AnimatedVisibility(visible = uiState.page == PageEnum.EDIT) {
                ZkTonyTopAppBar(
                    title = stringResource(id = R.string.edit),
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
        bottomBar = {
            AnimatedVisibility(visible = uiState.page == PageEnum.ADD) {
                ZkTonyBottomAddAppBar(
                    strings = uiState.entities.map { it.name },
                    insert = {
                        viewModel.insert(it)
                        viewModel.navigationTo(PageEnum.MAIN)
                    },
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
                    AnimatedVisibility(
                        visible = uiState.page == PageEnum.MAIN || uiState.page == PageEnum.ADD,
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally(),
                    ) {
                        CalibrationMainPage(
                            modifier = modifier,
                            uiState = uiState,
                            active = viewModel::active,
                            delete = viewModel::delete,
                            navigationTo = viewModel::navigationTo,
                            toggleSelected = viewModel::toggleSelected,
                        )
                    }

                    AnimatedVisibility(
                        visible = uiState.page == PageEnum.EDIT,
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally(),
                    ) {
                        CalibrationEditPage(
                            modifier = modifier,
                            addLiquid = viewModel::addLiquid,
                            entity = uiState.entities.find { it.id == uiState.selected }!!,
                            update = viewModel::update,
                        )
                    }
                }
            }
        }
    )
}