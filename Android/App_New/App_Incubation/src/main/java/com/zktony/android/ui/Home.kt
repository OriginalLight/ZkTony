package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.ui.components.HomeAppBar
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.launch


@Composable
fun HomeRoute(viewModel: HomeViewModel) {

    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.HOME -> { /* Do nothing */
                }

                else -> viewModel.uiEvent(HomeUiEvent.NavTo(PageType.HOME))
            }
        }
    }

    BackHandler { navigation() }

    LaunchedEffect(key1 = message) {
        if (message != null) {
            snackbarHostState.showSnackbar(
                message = message ?: "未知错误",
                actionLabel = "关闭",
                duration = SnackbarDuration.Short
            )
        }
    }

    HomeScreen(
        uiState = uiState,
        uiEvent = viewModel::uiEvent,
        navigation = navigation
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit,
    navigation: () -> Unit
) {
    Column {
        HomeAppBar(uiState) { navigation() }
    }
}