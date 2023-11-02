package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoDisturb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.ErrorDialog
import com.zktony.android.ui.components.HomeAppBar
import com.zktony.android.ui.components.ModuleItem
import com.zktony.android.ui.components.ProcessItem
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.JobState
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.toList
import com.zktony.android.utils.Constants
import com.zktony.android.utils.extra.dateFormat
import kotlinx.coroutines.launch


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeRoute(viewModel: HomeViewModel) {

    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    val message by viewModel.message.collectAsStateWithLifecycle()
    val page by viewModel.page.collectAsStateWithLifecycle()
    val uiFlags by viewModel.uiFlags.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val jobList by viewModel.jobList.collectAsStateWithLifecycle()
    val insulation by viewModel.insulation.collectAsStateWithLifecycle()
    val shaker by viewModel.shaker.collectAsStateWithLifecycle()

    val entities = viewModel.entities.collectAsLazyPagingItems()
    val navigation: () -> Unit = {
        scope.launch {
            when (page) {
                PageType.HOME -> {}
                else -> viewModel.dispatch(HomeIntent.NavTo(PageType.HOME))
            }
        }
    }

    BackHandler { navigation() }

    LaunchedEffect(key1 = message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dispatch(HomeIntent.Message(null))
        }
    }

    Column {
        HomeAppBar(page) { navigation() }
        ModuleList(selected, jobList, insulation, viewModel::dispatch)
        AnimatedContent(targetState = page) {
            when (page) {
                PageType.HOME -> HomeContent(
                    entities.toList(),
                    selected,
                    uiFlags,
                    jobList,
                    insulation,
                    shaker,
                    viewModel::dispatch
                )

                PageType.PROGRAM_LIST -> ProgramList(entities, selected, viewModel::dispatch)
                else -> {}
            }
        }
    }
}

@Composable
fun ModuleList(
    selected: Int,
    jobList: List<JobState>,
    insulation: List<Double>,
    dispatch: (HomeIntent) -> Unit
) {
    val size by rememberDataSaverState(key = Constants.ZT_0000, default = 4)
    val arrangement = if (size > 4) Arrangement.spacedBy(16.dp) else Arrangement.SpaceBetween

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = arrangement
    ) {
        items(size) { index ->
            ModuleItem(index, selected, jobList, insulation) {
                dispatch(
                    HomeIntent.ToggleSelected(
                        index
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeContent(
    entities: List<Program>,
    selected: Int,
    uiFlags: UiFlags,
    jobList: List<JobState>,
    insulation: List<Double>,
    shaker: Boolean,
    dispatch: (HomeIntent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current
    val navigationActions = LocalNavigationActions.current
    val jobState = jobList.find { it.index == selected } ?: JobState()

    if (uiFlags is UiFlags.Error) {
        ErrorDialog(
            message = uiFlags.message,
            onConfirm = { scope.launch { dispatch(HomeIntent.UiFlags(UiFlags.none())) } }
        )
    }

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
                                if (!jobState.isRunning()) {
                                    if (entities.isNotEmpty()) {
                                        dispatch(HomeIntent.NavTo(PageType.PROGRAM_LIST))
                                    } else {
                                        navigationActions.navigate(Route.PROGRAM)
                                    }
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
                                    if (uiFlags is UiFlags.None) {
                                        dispatch(HomeIntent.Start(selected))
                                    } else {
                                        snackbarHostState.showSnackbar("WARN 请先停止当前任务")
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
                                    dispatch(HomeIntent.Stop(selected))
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
                                    "INFO 当前抗体温度 ${insulation.getOrNull(0) ?: 0.0} ℃"
                                )
                            }
                        }
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    text = "${insulation.getOrNull(0) ?: 0.0} ℃",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        )
                        .clip(MaterialTheme.shapes.small)
                        .clickable { scope.launch { dispatch(HomeIntent.Shaker) } }
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    text = if (shaker) "ON" else "OFF",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun ProgramList(
    entities: LazyPagingItems<Program>,
    selected: Int,
    dispatch: (HomeIntent) -> Unit
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = Modifier.padding(16.dp),
        contentPadding = PaddingValues(16.dp),
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(entities) { index, item ->
            ListItem(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        scope.launch {
                            dispatch(HomeIntent.ToggleProcess(selected, item))
                            dispatch(HomeIntent.NavTo(PageType.HOME))
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
                leadingContent = {
                    Text(
                        text = "${index + 1}、",
                        style = MaterialTheme.typography.headlineSmall,
                        fontStyle = FontStyle.Italic
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}