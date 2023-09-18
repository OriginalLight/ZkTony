package com.zktony.android.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.*
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.*
import com.zktony.android.utils.Constants
import com.zktony.android.utils.extra.dateFormat
import kotlinx.coroutines.launch


@Composable
fun HomeRoute(viewModel: HomeViewModel) {

    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val entities = viewModel.entities.collectAsLazyPagingItems()
    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.HOME -> {}
                else -> viewModel.uiEvent(HomeUiEvent.NavTo(PageType.HOME))
            }
        }
    }

    LaunchedEffect(key1 = message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.uiEvent(HomeUiEvent.Message(null))
        }
    }

    HomeWrapper(
        entities = entities,
        uiState = uiState,
        uiEvent = viewModel::uiEvent,
        navigation = navigation
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeWrapper(
    entities: LazyPagingItems<Program>,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit,
    navigation: () -> Unit
) {
    Column {
        HomeAppBar(uiState) { navigation() }
        ModuleList(uiState, uiEvent)
        AnimatedContent(targetState = uiState.page) {
            when (uiState.page) {
                PageType.HOME -> HomeContent(entities.toList(), uiState, uiEvent)
                PageType.PROGRAM_LIST -> ProgramList(entities, uiState, uiEvent)
                else -> {}
            }
        }
    }
}

@Composable
fun ModuleList(
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val size by rememberDataSaverState(key = Constants.ZT_0000, default = 4)
    val arrangement = if (size > 4) Arrangement.spacedBy(16.dp) else Arrangement.SpaceBetween
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        state = lazyListState,
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = arrangement
    ) {
        items(size) { index ->
            ModuleItem(
                index = index,
                uiState = uiState,
                selected = uiState.selected,
                onClick = { uiEvent(HomeUiEvent.ToggleSelected(index)) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeContent(
    entities: List<Program>,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current
    val navigationActions = LocalNavigationActions.current
    val jobState = uiState.jobList.find { it.index == uiState.selected } ?: JobState()

    Row(
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(0.5f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            stickyHeader {
                val item = entities.find { it.id == jobState.id } ?: Program()
                ListItem(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            scope.launch {
                                if (entities.isNotEmpty()) {
                                    uiEvent(HomeUiEvent.NavTo(PageType.PROGRAM_LIST))
                                } else {
                                    navigationActions.navigate(Route.PROGRAM)
                                }
                            }
                        },
                    headlineContent = {
                        Text(
                            text = item.displayText,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    supportingContent = {
                        Text(
                            text = item.createTime.dateFormat("yyyy/MM/dd"),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    },
                    trailingContent = {
                        Icon(imageVector = Icons.Default.ArrowRight, contentDescription = null)
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }

            items(jobState.processes) { item ->
                ProcessItem(item = item)
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (jobState.status) {
                JobState.STOPPED, JobState.FINISHED -> {
                    Icon(
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape)
                            .clickable {
                                scope.launch {
                                    if (uiState.uiFlags == UiFlags.NONE && jobState.processes.isNotEmpty()) {
                                        uiEvent(HomeUiEvent.Start(uiState.selected))
                                    }
                                }
                            },
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Blue
                    )
                }

                JobState.RUNNING, JobState.WAITING -> {
                    Icon(
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape)
                            .clickable {
                                scope.launch {
                                    uiEvent(HomeUiEvent.Stop(uiState.selected))
                                }
                            },
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.Red
                    )
                }

                else -> {
                    Icon(
                        modifier = Modifier.size(128.dp),
                        imageVector = Icons.Default.DoDisturb,
                        contentDescription = null,
                        tint = Color.Red
                    )
                }
            }

            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        )
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "当前抗体保温温度 ${
                                        uiState.stand.insulation.getOrNull(
                                            0
                                        ) ?: 0.0
                                    } ℃",
                                    actionLabel = "关闭",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    text = "${uiState.stand.insulation.getOrNull(0) ?: 0.0} ℃",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        )
                        .clip(MaterialTheme.shapes.small)
                        .clickable { scope.launch { uiEvent(HomeUiEvent.Shaker) } }
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    text = if (uiState.stand.shaker) "ON" else "OFF",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun ProgramList(
    entities: LazyPagingItems<Program>,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = Modifier,
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(entities) { index, item ->
            ProgramItem(
                index = index,
                item = item,
                selected = false
            ) {
                scope.launch {
                    uiEvent(HomeUiEvent.ToggleProcess(uiState.selected, item))
                    uiEvent(HomeUiEvent.NavTo(PageType.HOME))
                }
            }
        }
    }
}