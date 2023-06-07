package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.ui.components.ZktyTopAppBar
import com.zktony.android.ui.utils.PageType


@Composable
fun ZktyHome(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ZktyHomeViewModel,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val page = remember { mutableStateOf(PageType.MENU) }
    val content = LocalContext.current

    BackHandler {
        when (page.value) {
            PageType.MENU -> {}
            else -> page.value = PageType.MENU
        }
    }

    Column(modifier = modifier) {
        // app bar
        AnimatedVisibility(visible = page.value == PageType.MENU) {
            ZktyTopAppBar(
                title = stringResource(id = R.string.tab_home),
                navigation = { page.value = PageType.MENU },
            )
        }
        // menu
        AnimatedVisibility(visible = page.value == PageType.MENU) {

        }
        // select
        AnimatedVisibility(visible = page.value == PageType.SELECT) {
            
        }
    }
}
