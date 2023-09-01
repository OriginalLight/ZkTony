package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.ProcessItem
import com.zktony.android.ui.components.ProgramAppBar
import com.zktony.android.ui.components.ProgramItem
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.launch

@Composable
fun ProgramRoute(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ProgramViewModel,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val entities = viewModel.entities.collectAsLazyPagingItems()
    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.PROGRAM_LIST -> navController.navigateUp()
                else -> viewModel.uiEvent(ProgramUiEvent.NavTo(PageType.PROGRAM_LIST))
            }
        }
    }

    BackHandler { navigation() }

    Scaffold(
        topBar = {
            ProgramAppBar(
                entities = entities,
                uiState = uiState,
                uiEvent = viewModel::uiEvent,
                snackbarHostState = snackbarHostState
            ) { navigation() }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
    ) { paddingValues ->
        ProgramScreen(
            modifier = modifier.padding(paddingValues),
            entities = entities,
            uiState = uiState,
            uiEvent = viewModel::uiEvent
        )
    }
}


@Composable
fun ProgramScreen(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<Program>,
    uiState: ProgramUiState,
    uiEvent: (ProgramUiEvent) -> Unit
) {
    AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_LIST) {
        ProgramList(modifier, entities, uiState, uiEvent)
    }

    AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_DETAIL) {
        ProgramDetail(modifier, entities, uiState, uiEvent)
    }
}

@Composable
fun ProgramList(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<Program>,
    uiState: ProgramUiState,
    uiEvent: (ProgramUiEvent) -> Unit,
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            count = entities.itemCount,
            key = entities.itemKey(),
            contentType = entities.itemContentType()
        ) { index ->
            val item = entities[index]
            if (item != null) {
                val color =
                    if (uiState.selected == item.id) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }

                ProgramItem(
                    modifier = Modifier.background(
                        color = color,
                        shape = MaterialTheme.shapes.medium
                    ),
                    index = index,
                    program = item,
                    onClick = {
                        scope.launch {
                            if (uiState.selected == 0L) {
                                uiEvent(ProgramUiEvent.ToggleSelected(it.id))
                            } else {
                                uiEvent(ProgramUiEvent.ToggleSelected(0L))
                            }
                        }
                    },
                    onDoubleClick = {
                        scope.launch {
                            uiEvent(ProgramUiEvent.ToggleSelected(it.id))
                            uiEvent(ProgramUiEvent.NavTo(PageType.PROGRAM_DETAIL))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ProgramDetail(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<Program>,
    uiState: ProgramUiState,
    uiEvent: (ProgramUiEvent) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val selected = entities.itemSnapshotList.items.find { it.id == uiState.selected } ?: Program()
    var selectedIndex by remember { mutableIntStateOf(0) }

    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(0.5f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(selected.processes) { index, process ->
                val color = if (index == selectedIndex) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }

                ProcessItem(
                    modifier = Modifier.background(
                        color = color,
                        shape = MaterialTheme.shapes.medium
                    ),
                    process = process,
                    onClick = { selectedIndex = index }
                ) {
                    scope.launch {
                        val processes = selected.processes.toMutableList()
                        processes.removeAt(index)
                        uiEvent(ProgramUiEvent.Update(selected.copy(processes = processes)))
                    }
                }
            }
        }
    }
}