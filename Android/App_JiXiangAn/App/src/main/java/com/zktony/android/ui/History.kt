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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.ui.components.HistoryAppBar
import com.zktony.android.ui.components.HistoryItem
import com.zktony.android.ui.components.LogItem
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.room.entities.History
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/8/31 9:57
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HistoryRoute(viewModel: HistoryViewModel) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current

    val page by viewModel.page.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()

    val entities = viewModel.entities.collectAsLazyPagingItems()
    val navigation: () -> Unit = {
        scope.launch {
            when (page) {
                PageType.HISTORY_LIST -> navigationActions.navigateUp()
                else -> viewModel.dispatch(HistoryIntent.NavTo(PageType.HISTORY_LIST))
            }
        }
    }

    BackHandler { navigation() }

    Column {
        HistoryAppBar(entities, selected, page, viewModel::dispatch) { navigation() }
        AnimatedContent(targetState = page) {
            when (it) {
                PageType.HISTORY_LIST -> HistoryList(entities, viewModel::dispatch)
                PageType.HISTORY_DETAIL -> HistoryDetail(entities, selected)
                else -> {}
            }
        }
    }
}

@Composable
fun HistoryList(
    entities: LazyPagingItems<History>,
    dispatch: (HistoryIntent) -> Unit
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
                    dispatch(HistoryIntent.Selected(it.id))
                    dispatch(HistoryIntent.NavTo(PageType.HISTORY_DETAIL))
                }
            }
        }
    }
}

@Composable
fun HistoryDetail(
    entities: LazyPagingItems<History>,
    selected: Long
) {
    LazyColumn(
        modifier = Modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val item = entities.itemSnapshotList.items.find { it.id == selected }
        if (item != null) {
            items(item.logs) { LogItem(item = it) }
        }
    }
}