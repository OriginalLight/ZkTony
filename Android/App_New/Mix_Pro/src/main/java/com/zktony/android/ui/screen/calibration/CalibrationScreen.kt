package com.zktony.android.ui.screen.calibration

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.ui.viewmodel.CalibrationPage
import com.zktony.android.ui.viewmodel.CalibrationViewModel

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
    var selected by remember { mutableStateOf(0L) }

    BackHandler {
        if (uiState.page == CalibrationPage.CALIBRATION) {
            navController.navigateUp()
        } else {
            viewModel.navigateTo(CalibrationPage.CALIBRATION)
        }
    }

    AnimatedVisibility(visible = uiState.page == CalibrationPage.CALIBRATION) {
        CalibrationPage(
            modifier = modifier,
            selected = selected,
            delete = viewModel::delete,
            enable = { viewModel.enable(selected) },
            list = uiState.list,
            navigationTo = viewModel::navigateTo,
            toggleSelected = { selected = if (selected == it) 0L else it },
        )
    }

    AnimatedVisibility(visible = uiState.page == CalibrationPage.CALIBRATION_ADD) {
        CalibrationAddPage(
            modifier = modifier,
            insert = viewModel::insert,
            list = uiState.list,
            navigationTo = viewModel::navigateTo,
            toggleSelected = { selected = it },
        )
    }

    AnimatedVisibility(
        visible = uiState.page == CalibrationPage.CALIBRATION_EDIT,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        CalibrationEditPage(
            modifier = modifier,
            addLiquid = viewModel::addLiquid,
            delete = viewModel::deleteData,
            insert = viewModel::insertData,
            list = viewModel.dataList(selected),
            navigationTo = viewModel::navigateTo,
            selected = selected,
        )
    }
}