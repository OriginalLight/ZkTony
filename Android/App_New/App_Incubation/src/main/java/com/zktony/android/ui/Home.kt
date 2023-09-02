package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.ui.components.HomeAppBar
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.launch


@Composable
fun HomeRoute(viewModel: HomeViewModel) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.START -> viewModel.uiEvent(HomeUiEvent.NavTo(PageType.LIST))
                else -> {}
            }
        }
    }

    BackHandler { navigation() }

    HomeScreen(
        navigationActions = navigationActions,
        uiState = uiState,
        uiEvent = viewModel::uiEvent,
        snackbarHostState = snackbarHostState,
        navigation = navigation
    )
}

@Composable
fun HomeScreen(
    navigationActions: NavigationActions,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    navigation: () -> Unit
) {
    Column {
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
    }

}