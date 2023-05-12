package com.zktony.android.ui.screen.calibration

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
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
    val entities by viewModel.entities().collectAsState(emptyList())
    var index by remember { mutableStateOf(-1) }
    var page by remember { mutableStateOf(PageEnum.MAIN) }


    BackHandler {
        if (page == PageEnum.MAIN) {
            navController.navigateUp()
        } else {
            page = PageEnum.MAIN
        }
    }

    AnimatedVisibility(
        visible = page == PageEnum.MAIN,
        enter = expandHorizontally(),
        exit = shrinkHorizontally(),
    ) {
        CalibrationMainPage(
            modifier = modifier,
            active = viewModel::active,
            delete = viewModel::delete,
            entities = entities,
            index = index,
            insert = viewModel::insert,
            navigationTo = { page = it },
            toggleIndex = { index = it },
        )
    }

    AnimatedVisibility(
        visible = page == PageEnum.EDIT,
        enter = expandHorizontally(),
        exit = shrinkHorizontally(),
    ) {
        CalibrationEditPage(
            modifier = modifier,
            addLiquid = viewModel::addLiquid,
            entity = entities[index],
            navigationTo = { page = it },
            update = viewModel::update,
        )
    }
}