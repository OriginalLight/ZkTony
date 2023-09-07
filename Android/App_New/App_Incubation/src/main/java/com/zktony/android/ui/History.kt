package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.data.entities.History
import com.zktony.android.ui.components.HistoryAppBar
import com.zktony.android.ui.components.HistoryItem
import com.zktony.android.ui.components.LogItem
import com.zktony.android.ui.utils.*
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/8/31 9:57
 */
@Composable
fun HistoryRoute(viewModel: HistoryViewModel) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val entities = viewModel.entities.collectAsLazyPagingItems()
    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.HISTORY_LIST -> navigationActions.navigateUp()
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

    HistoryWrapper(
        entities = entities,
        uiState = uiState,
        uiEvent = viewModel::uiEvent,
        navigation = navigation
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HistoryWrapper(
    entities: LazyPagingItems<History>,
    uiState: HistoryUiState,
    uiEvent: (HistoryUiEvent) -> Unit,
    navigation: () -> Unit
) {
    Column {
        HistoryAppBar(entities, uiState, uiEvent) { navigation() }
        AnimatedContent(targetState = uiState.page) {
            when (it) {
                PageType.HISTORY_LIST -> HistoryList(entities, uiEvent)
                PageType.HISTORY_DETAIL -> HistoryDetail(entities, uiState)
                else -> {}
            }
        }
    }
}

@Composable
fun HistoryList(
    entities: LazyPagingItems<History>,
    uiEvent: (HistoryUiEvent) -> Unit
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = Modifier,
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(entities) { index, item ->
            HistoryItem(index, item) {
                scope.launch {
                    uiEvent(HistoryUiEvent.ToggleSelected(it.id))
                    uiEvent(HistoryUiEvent.NavTo(PageType.HISTORY_DETAIL))
                }
            }
        }
    }
}

@Composable
fun HistoryDetail(
    entities: LazyPagingItems<History>,
    uiState: HistoryUiState
) {
    LazyColumn(
        modifier = Modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val item = entities.itemSnapshotList.items.find { it.id == uiState.selected }
        if (item != null) {
            items(item.logs) { LogItem(item = it) }
        }
    }
}