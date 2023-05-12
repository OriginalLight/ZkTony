package com.zktony.android.ui.screen.container

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
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
    val entities by viewModel.entities().collectAsStateWithLifecycle(emptyList())
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
        ContainerMainPage(
            modifier = modifier,
            entities = entities,
            index = index,
            insert = viewModel::insert,
            navigationTo = { page = it },
            toggleIndex = { index = it },
            delete = viewModel::delete,
        )
    }

    AnimatedVisibility(
        visible = page == PageEnum.EDIT,
        enter = expandHorizontally(),
        exit = shrinkHorizontally(),
    ) {
        ContainerEditPage(
            modifier = modifier,
            entity = entities[index],
            navigationTo = { page = it },
            update = viewModel::update,
        )
    }
}
