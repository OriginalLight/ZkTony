package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.zktony.android.data.entities.History
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
        topBar = {},
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
    LazyColumn {
        items(
            count = entities.itemCount,
            key = entities.itemKey(),
            contentType = entities.itemContentType()
        ) {

            entities[it]?.let {
                Text(text = it.id.toString())
            }
        }
    }
}