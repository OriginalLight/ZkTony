package com.zktony.android.ui.screen.container

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

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

    AnimatedVisibility(
        visible = uiState.page == ContainerPage.CONTAINER,
        enter = expandHorizontally(),
        exit = shrinkHorizontally(),
    ) {
        ContainerPage(
            modifier = modifier,
            delete = viewModel::delete,
            list = uiState.list,
            navigationTo = viewModel::navigationTo,
            selected = selected,
            toggleSelected = { selected = if (selected == it) 0L else it },
        )
    }

    AnimatedVisibility(
        visible = uiState.page == ContainerPage.CONTAINER_ADD,
        enter = expandHorizontally(),
        exit = shrinkHorizontally(),
    ) {
        ContainerAddPage(
            modifier = modifier,
            insert = viewModel::insert,
            list = uiState.list,
            navigationTo = viewModel::navigationTo,
        )
    }

    AnimatedVisibility(
        visible = uiState.page == ContainerPage.CONTAINER_EDIT,
        enter = expandHorizontally(),
        exit = shrinkHorizontally(),
    ) {
        ContainerEditPage(
            modifier = modifier,
            entityFlow = viewModel.entityFlow(selected),
            navigationTo = viewModel::navigationTo,
            update = viewModel::update,
        )
    }
}
