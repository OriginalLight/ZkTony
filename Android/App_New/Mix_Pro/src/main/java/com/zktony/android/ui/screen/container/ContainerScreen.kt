package com.zktony.android.ui.screen.container

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
import com.zktony.android.ui.viewmodel.ContainerPage
import com.zktony.android.ui.viewmodel.ContainerViewModel

/**
 * Container screen
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param viewModel ContainerViewModel
 * @return Unit
 */
@Composable
fun ContainerScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ContainerViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf(0L) }

    BackHandler {
        if (uiState.page == ContainerPage.CONTAINER) {
            navController.navigateUp()
        } else {
            viewModel.navigationTo(ContainerPage.CONTAINER)
        }
    }

    AnimatedVisibility(visible = uiState.page == ContainerPage.CONTAINER) {
        ContainerPage(
            modifier = modifier,
            delete = viewModel::delete,
            list = uiState.list,
            navigationTo = viewModel::navigationTo,
            selected = selected,
            toggleSelected = { selected = if (selected == it) 0L else it },
        )
    }

    AnimatedVisibility(visible = uiState.page == ContainerPage.CONTAINER_ADD) {
        ContainerAddPage(
            modifier = modifier,
            insert = viewModel::insert,
            list = uiState.list,
            navigationTo = viewModel::navigationTo,
            toggleSelected = { selected = it },
        )
    }
}
