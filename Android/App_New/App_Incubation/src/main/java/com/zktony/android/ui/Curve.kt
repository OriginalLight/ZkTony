package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.zktony.android.data.entities.Curve
import com.zktony.android.ui.components.*
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/8/30 11:10
 */

@Composable
fun CurveRoute(
    navController: NavHostController,
    viewModel: CurveViewModel,
    snackbarHostState: SnackbarHostState
) {

    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val entities = viewModel.entities.collectAsLazyPagingItems()
    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.CURVE_LIST -> navController.navigateUp()
                else -> viewModel.uiEvent(CurveUiEvent.NavTo(PageType.CURVE_LIST))
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
        topBar = { CurveAppBar(navigation = navigation) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
    ) { paddingValues ->
        CurveScreen(
            modifier = Modifier.padding(paddingValues),
            entities = entities,
            uiState = uiState,
            uiEvent = viewModel::uiEvent
        )
    }
}

@Composable
fun CurveScreen(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<Curve>,
    uiState: CurveUiState,
    uiEvent: (CurveUiEvent) -> Unit,
) {
    LaunchedEffect(key1 = entities.itemCount) {
        if (uiState.selected == 0L && entities.itemCount > 0) {
            uiEvent(CurveUiEvent.ToggleSelected(entities[0]!!.id))
        }
    }

    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CurveList(modifier.weight(1f), entities, uiState, uiEvent)
        CurveDetail(modifier.weight(1f), entities, uiState, uiEvent)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CurveList(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<Curve>,
    uiState: CurveUiState,
    uiEvent: (CurveUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stickyHeader { CurveListHeader(uiState = uiState, uiEvent = uiEvent) }
        items(
            count = entities.itemCount,
            key = entities.itemKey(),
            contentType = entities.itemContentType()
        ) { index ->
            val item = entities[index]
            if (item != null) {
                val color =
                    if (uiState.selected == item.id) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                CurveItem(
                    modifier = Modifier.background(
                        color = color,
                        shape = MaterialTheme.shapes.medium
                    ),
                    index = index,
                    curve = item,
                    uiEvent = uiEvent
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun CurveDetail(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<Curve>,
    uiState: CurveUiState,
    uiEvent: (CurveUiEvent) -> Unit,
) {
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier.windowInsetsPadding(WindowInsets.imeAnimationSource),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val selected = entities.itemSnapshotList.items.find { it.id == uiState.selected }
        if (selected != null) {
            stickyHeader { CurvePointHeader(curve = selected, uiEvent = uiEvent) }
            items(items = selected.points) { item ->
                PointItem(
                    point = item,
                    uiState = uiState,
                    onClickOne = {},
                    onClickTwo = { point ->
                        scope.launch {
                            val points = selected.points.toMutableList()
                            points -= point
                            uiEvent(CurveUiEvent.Update(selected.copy(points = points)))
                        }
                    },
                    onPointChange = { point ->
                        scope.launch {
                            val points = selected.points.toMutableList()
                            val index = points.indexOf(item)
                            points[index] = point
                            uiEvent(CurveUiEvent.Update(selected.copy(points = points)))
                        }
                    }
                )
            }
        }
    }
}