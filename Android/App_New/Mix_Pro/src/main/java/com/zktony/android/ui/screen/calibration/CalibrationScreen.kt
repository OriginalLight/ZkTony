package com.zktony.android.ui.screen.calibration

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

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

    BackHandler {
        if (uiState.page == CalibrationPageEnum.CALIBRATION) {
            navController.navigateUp()
        } else {
            viewModel.navigateTo(CalibrationPageEnum.CALIBRATION)
        }
    }

    AnimatedVisibility(
        visible = uiState.page == CalibrationPageEnum.CALIBRATION,
        enter = expandHorizontally(),
        exit = shrinkHorizontally(),
    ) {
        CalibrationPage(
            modifier = modifier,
            activeEntity = viewModel::activeEntity,
            delete = viewModel::delete,
            entity = uiState.entity,
            list = uiState.list,
            navigationTo = viewModel::navigateTo,
            toggleEntity = viewModel::toggleEntity,
        )
    }

    AnimatedVisibility(
        visible = uiState.page == CalibrationPageEnum.CALIBRATION_ADD,
        enter = expandHorizontally(),
        exit = shrinkHorizontally(),
    ) {
        CalibrationAddPage(
            modifier = modifier,
            insert = viewModel::insert,
            list = uiState.list,
            navigationTo = viewModel::navigateTo,
        )
    }

    AnimatedVisibility(
        visible = uiState.page == CalibrationPageEnum.CALIBRATION_EDIT,
        enter = expandHorizontally(),
        exit = shrinkHorizontally(),
    ) {
        CalibrationEditPage(
            modifier = modifier,
            addLiquid = viewModel::addLiquid,
            entity = uiState.entity,
            navigationTo = viewModel::navigateTo,
            update = viewModel::update,
        )
    }
}