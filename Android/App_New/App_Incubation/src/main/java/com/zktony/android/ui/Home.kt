package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.ui.components.HomeAppBar
import com.zktony.android.ui.components.ModuleItem
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.Constants
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
        ModuleList(uiState, uiEvent)
    }
}

@Composable
fun ModuleList(
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val size by rememberDataSaverState(key = Constants.ZT_0000, default = 4)
    var selected by remember { mutableIntStateOf(0) }
    val arrangement = if (size > 4) Arrangement.spacedBy(16.dp) else Arrangement.SpaceBetween
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        state = lazyListState,
        contentPadding = PaddingValues(32.dp),
        horizontalArrangement = arrangement
    ) {
        items(size) { index ->
            ModuleItem(index = index, selected = selected == index)
        }
    }
}