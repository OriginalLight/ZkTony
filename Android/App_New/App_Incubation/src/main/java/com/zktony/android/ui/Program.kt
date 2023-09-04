package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.ProcessItem
import com.zktony.android.ui.components.ProgramAppBar
import com.zktony.android.ui.components.ProgramItem
import com.zktony.android.ui.utils.*
import kotlinx.coroutines.launch

@Composable
fun ProgramRoute(viewModel: ProgramViewModel) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val entities = viewModel.entities.collectAsLazyPagingItems()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.PROGRAM_LIST -> navigationActions.navigateUp()
                else -> viewModel.uiEvent(ProgramUiEvent.NavTo(PageType.PROGRAM_LIST))
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

    ProgramScreen(
        entities = entities,
        uiState = uiState,
        uiEvent = viewModel::uiEvent,
        navigation = navigation
    )
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProgramScreen(
    entities: LazyPagingItems<Program>,
    uiState: ProgramUiState,
    uiEvent: (ProgramUiEvent) -> Unit,
    navigation: () -> Unit
) {
    Column {
        ProgramAppBar(entities.toList(), uiState, uiEvent) { navigation() }
        AnimatedContent(targetState = uiState.page) {
            when (it) {
                PageType.PROGRAM_LIST -> ProgramList(entities, uiState, uiEvent)
                PageType.PROGRAM_DETAIL -> ProgramDetail(entities.toList(), uiState, uiEvent)
                else -> {}
            }
        }
    }
}

@Composable
fun ProgramList(
    entities: LazyPagingItems<Program>,
    uiState: ProgramUiState,
    uiEvent: (ProgramUiEvent) -> Unit,
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = Modifier,
        contentPadding = PaddingValues(16.dp),
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(entities) { index, item ->
            ProgramItem(
                index = index,
                item = item,
                selected = uiState.selected == item.id
            ) { double ->
                scope.launch {
                    if (double) {
                        uiEvent(ProgramUiEvent.ToggleSelected(item.id))
                        uiEvent(ProgramUiEvent.NavTo(PageType.PROGRAM_DETAIL))
                    } else {
                        if (uiState.selected != item.id) {
                            uiEvent(ProgramUiEvent.ToggleSelected(item.id))
                        } else {
                            uiEvent(ProgramUiEvent.ToggleSelected(0L))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgramDetail(
    entities: List<Program>,
    uiState: ProgramUiState,
    uiEvent: (ProgramUiEvent) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val selected = entities.find { it.id == uiState.selected } ?: Program()
    var selectedIndex by remember { mutableIntStateOf(0) }

    Row(
        modifier = Modifier.padding(16.dp),
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
                    item = process,
                    onClick = { selectedIndex = index },
                    onDelete = {
                        scope.launch {
                            val processes = selected.processes.toMutableList()
                            processes.removeAt(index)
                            uiEvent(ProgramUiEvent.Update(selected.copy(processes = processes)))
                        }
                    },
                    onUpOrDown = {
                        scope.launch {
                            val processes = selected.processes.toMutableList()
                            val temp = processes[index]
                            if (it) {
                                if (index == 0) {
                                    return@launch
                                }
                                processes[index] = processes[index - 1]
                                processes[index - 1] = temp
                            } else {
                                if (index == processes.size - 1) {
                                    return@launch
                                }
                                processes[index] = processes[index + 1]
                                processes[index + 1] = temp
                            }
                            uiEvent(ProgramUiEvent.Update(selected.copy(processes = processes)))
                        }
                    }
                )
            }
        }
    }
}