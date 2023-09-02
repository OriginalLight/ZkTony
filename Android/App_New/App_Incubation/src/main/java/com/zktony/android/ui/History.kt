package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.zktony.android.data.entities.History
import com.zktony.android.ui.components.HistoryAppBar
import com.zktony.android.ui.components.HistoryItem
import com.zktony.android.ui.components.LogItem
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/8/31 9:57
 */
@Composable
fun HistoryRoute(
    navController: NavHostController,
    viewModel: HistoryViewModel,
    snackbarHostState: SnackbarHostState
) {

    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val entities = viewModel.entities.collectAsLazyPagingItems()
    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.HISTORY_LIST -> navController.navigateUp()
                else -> viewModel.uiEvent(HistoryUiEvent.NavTo(PageType.HISTORY_LIST))
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

    Scaffold(
        topBar = {
            HistoryAppBar(
                entities = entities,
                uiState = uiState,
                uiEvent = viewModel::uiEvent
            ) { navigation() }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
    ) { paddingValues ->
        HistoryScreen(
            modifier = Modifier.padding(paddingValues),
            entities = entities,
            uiState = uiState,
            uiEvent = viewModel::uiEvent
        )
    }
}

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<History>,
    uiState: HistoryUiState,
    uiEvent: (HistoryUiEvent) -> Unit
) {
    AnimatedVisibility(visible = uiState.page == PageType.HISTORY_LIST) {
        HistoryList(modifier, entities, uiEvent)
    }

    AnimatedVisibility(visible = uiState.page == PageType.HISTORY_DETAIL) {
        HistoryDetail(modifier, entities, uiState)
    }

}

@Composable
fun HistoryList(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<History>,
    uiEvent: (HistoryUiEvent) -> Unit
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            count = entities.itemCount,
            key = entities.itemKey(),
            contentType = entities.itemContentType()
        ) { index ->
            val item = entities[index]
            if (item != null) {
                HistoryItem(index, item) {
                    scope.launch {
                        uiEvent(HistoryUiEvent.ToggleSelected(it.id))
                        uiEvent(HistoryUiEvent.NavTo(PageType.HISTORY_DETAIL))
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryDetail(
    modifier: Modifier,
    entities: LazyPagingItems<History>,
    uiState: HistoryUiState
) {

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val item = entities.itemSnapshotList.items.find { it.id == uiState.selected }
        if (item != null) {
            items(item.logs) { LogItem(item = it) }
        }
    }
}
