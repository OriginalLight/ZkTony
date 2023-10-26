package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.zktony.android.ui.components.CountDownDialog
import com.zktony.android.ui.components.HomeAppBar
import com.zktony.android.ui.components.OrificePlate
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.toList
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils.start
import com.zktony.android.utils.extra.dateFormat
import kotlinx.coroutines.launch

@Composable
fun HomeRoute(viewModel: HomeViewModel) {

    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val entities = viewModel.entities.collectAsLazyPagingItems()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.PROGRAM_LIST -> viewModel.uiEvent(HomeUiEvent.NavTo(PageType.HOME))
                else -> {}
            }
        }
    }

    BackHandler { navigation() }

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
        AnimatedContent(targetState = uiState.page) {
            when (uiState.page) {
                PageType.PROGRAM_LIST -> ProgramList(entities, uiEvent)
                PageType.HOME -> HomeContent(entities.toList(), uiState, uiEvent)
                else -> {}
            }
        }
    }
}

@Composable
fun ProgramList(
    entities: LazyPagingItems<Program>,
    uiEvent: (HomeUiEvent) -> Unit,
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
                            uiEvent(HomeUiEvent.ToggleSelected(item.id))
                            uiEvent(HomeUiEvent.NavTo(PageType.HOME))
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeContent(
    entities: List<Program>,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit
) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current
    var dialog by remember { mutableStateOf(false) }
    val info by remember(uiState.jobState.orificePlate) {
        mutableStateOf(uiState.jobState.orificePlate.getInfo())
    }

    LaunchedEffect(key1 = entities) {
        if (entities.isEmpty()) {
            uiEvent(HomeUiEvent.ToggleSelected(0L))
        } else {
            if (uiState.selected == 0L) {
                uiEvent(HomeUiEvent.ToggleSelected(entities.getOrNull(0)?.id ?: 0L))
            } else {
                if (!entities.any { it.id == uiState.selected }) {
                    uiEvent(HomeUiEvent.ToggleSelected(entities.getOrNull(0)?.id ?: 0L))
                }
            }
        }
    }

    if (dialog) {
        CountDownDialog(
            onStart = {
                scope.launch {
                    uiEvent(HomeUiEvent.Pipeline(1))
                }
            },
            onStop = {
                scope.launch {
                    uiEvent(HomeUiEvent.Pipeline(0))
                }
            },
            onCancel = {
                dialog = false
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.5f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val selected = entities.firstOrNull { it.id == uiState.selected } ?: Program()

            ListItem(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        scope.launch {
                            if (uiState.jobState.status == JobState.STEPPED) {
                                if (entities.isNotEmpty()) {
                                    uiEvent(HomeUiEvent.NavTo(PageType.PROGRAM_LIST))
                                } else {
                                    navigationActions.navigate(Route.PROGRAM)
                                }
                            }
                        }
                    },
                headlineContent = {
                    Text(
                        text = selected.displayText,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        info.forEach {
                            Text(
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = Color.Gray,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                text = it,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                trailingContent = {
                    Icon(imageVector = Icons.Default.ArrowRight, contentDescription = null)
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )

            OrificePlate(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(328.dp),
                row = uiState.jobState.orificePlate.row,
                column = uiState.jobState.orificePlate.column,
                selected = uiState.jobState.finished
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .clickable {
                            scope.launch {
                                if (uiState.uiFlags == UiFlags.NONE && uiState.selected != 0L) {
                                    when (uiState.jobState.status) {
                                        JobState.STEPPED -> {
                                            uiEvent(HomeUiEvent.Start)
                                            snackbarHostState.showSnackbar("程序开始")
                                        }

                                        JobState.RUNNING -> {
                                            uiEvent(HomeUiEvent.Pause)
                                            snackbarHostState.showSnackbar("程序暂停")
                                        }

                                        JobState.PAUSED -> {
                                            uiEvent(HomeUiEvent.Resume)
                                            snackbarHostState.showSnackbar("程序继续")
                                        }
                                    }
                                } else {
                                    snackbarHostState.showSnackbar("请中止其他操作或选择程序")
                                }
                            }
                        },
                    imageVector = if (uiState.jobState.status == JobState.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = when (uiState.jobState.status) {
                        JobState.STEPPED -> Color.Blue
                        JobState.RUNNING -> Color.DarkGray
                        JobState.PAUSED -> Color.Yellow
                        else -> Color.Gray
                    }
                )

                if (uiState.jobState.status != JobState.STEPPED) {
                    Icon(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .clickable {
                                scope.launch {
                                    uiEvent(HomeUiEvent.Stop)
                                    snackbarHostState.showSnackbar("程序中止")
                                }
                            },
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.Red
                    )
                }
            }

            if (uiState.jobState.status != JobState.RUNNING) {
                Column(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                                    if (uiState.uiFlags == UiFlags.NONE) {
                                        uiEvent(HomeUiEvent.Reset)
                                    }
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        text = if (uiState.uiFlags == UiFlags.RESET) "复位中" else "复位",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uiState.uiFlags == UiFlags.RESET) Color.Red else Color.Unspecified
                    )

                    Text(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                scope.launch {
                                    if (uiState.uiFlags == UiFlags.NONE) {
                                        dialog = true
                                    }
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        text = "清洗",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                scope.launch {
                                    if (uiState.uiFlags == UiFlags.NONE || uiState.uiFlags == UiFlags.PIPELINE_IN) {
                                        if (uiState.uiFlags == UiFlags.NONE) {
                                            uiEvent(HomeUiEvent.Pipeline(2))
                                        } else {
                                            uiEvent(HomeUiEvent.Pipeline(0))
                                        }
                                    }
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        text = if (uiState.uiFlags == UiFlags.PIPELINE_IN) "填充中" else "填充",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uiState.uiFlags == UiFlags.PIPELINE_IN) Color.Red else Color.Unspecified
                    )

                    Text(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                scope.launch {
                                    if (uiState.uiFlags == UiFlags.NONE || uiState.uiFlags == UiFlags.PIPELINE_OUT) {
                                        if (uiState.uiFlags == UiFlags.NONE) {
                                            uiEvent(HomeUiEvent.Pipeline(3))
                                        } else {
                                            uiEvent(HomeUiEvent.Pipeline(0))
                                        }
                                    }
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        text = if (uiState.uiFlags == UiFlags.PIPELINE_OUT) "回吸中" else "回吸",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uiState.uiFlags == UiFlags.PIPELINE_OUT) Color.Red else Color.Unspecified
                    )

                    val tankAbscissa by rememberDataSaverState(
                        key = Constants.ZT_0003,
                        default = 0.0
                    )
                    val tankOrdinate by rememberDataSaverState(
                        key = Constants.ZT_0004,
                        default = 0.0
                    )

                    Text(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                scope.launch {
                                    start {
                                        with(index = 0, pdv = tankAbscissa)
                                        with(index = 1, pdv = tankOrdinate)
                                    }
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        text = "废液槽",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}