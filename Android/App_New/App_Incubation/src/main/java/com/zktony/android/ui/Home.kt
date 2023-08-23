package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.ui.components.HomeAppBar
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.launch


@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: HomeViewModel,
    navigationActions: NavigationActions,
    snackbarHostState: SnackbarHostState,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.START -> viewModel.uiEvent(HomeUiEvent.NavTo(PageType.LIST))
                else -> {}
            }
        }
    }

    BackHandler { navigation() }

    Scaffold(
        topBar = {
            HomeAppBar(navigationActions = navigationActions) {
                AnimatedVisibility(visible = uiState.page != PageType.LIST) {
                    ElevatedButton(onClick = navigation) {
                        Icon(
                            imageVector = Icons.Default.Reply,
                            contentDescription = null
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
    ) { paddingValues ->
        HomeScreen(
            modifier = modifier.padding(paddingValues),
            navController = navController,
            uiState = uiState,
            uiEvent = viewModel::uiEvent
        )
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit,
) {

}