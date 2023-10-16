package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.HomeAppBar
import com.zktony.android.ui.components.JobActionCard
import com.zktony.android.ui.components.OrificePlate
import com.zktony.android.ui.components.ProgramItem
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.toList
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
        HomeActions(uiState, uiEvent)
        AnimatedContent(targetState = uiState.page) {
            when (uiState.page) {
                PageType.PROGRAM_LIST -> ProgramList(entities, uiState, uiEvent)
                PageType.HOME -> JobContent(entities.toList(), uiState, uiEvent)
                else -> {}
            }
        }

    }
}

@Composable
fun HomeActions(
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit
) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ElevatedButton(
            enabled = uiState.uiFlags == UiFlags.NONE || uiState.uiFlags == UiFlags.RESET,
            onClick = {
                if (uiState.jobState.status != JobState.RUNNING) {
                    uiEvent(HomeUiEvent.Reset)
                }
            }) {
            if (uiState.uiFlags == UiFlags.RESET) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null
                )
            }
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "复位"
            )
        }

        ElevatedButton(
            enabled = uiState.uiFlags == UiFlags.NONE || uiState.uiFlags == UiFlags.CLEAN,
            onClick = {
                scope.launch {
                    if (uiState.jobState.status != JobState.RUNNING) {
                        if (uiState.uiFlags == UiFlags.NONE) {
                            uiEvent(HomeUiEvent.Pipeline(1))
                        } else if (uiState.uiFlags == UiFlags.CLEAN) {
                            uiEvent(HomeUiEvent.Pipeline(0))
                        }
                    }
                }
            }) {
            if (uiState.uiFlags == UiFlags.CLEAN) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Default.ClearAll,
                    contentDescription = null
                )
            }
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "清洗"
            )
        }

        ElevatedButton(
            enabled = uiState.uiFlags == UiFlags.NONE || uiState.uiFlags == UiFlags.PIPELINE_IN,
            onClick = {
                scope.launch {
                    if (uiState.jobState.status != JobState.RUNNING) {
                        if (uiState.uiFlags == UiFlags.NONE) {
                            uiEvent(HomeUiEvent.Pipeline(2))
                        } else if (uiState.uiFlags == UiFlags.PIPELINE_IN) {
                            uiEvent(HomeUiEvent.Pipeline(0))
                        }
                    }
                }
            }) {
            if (uiState.uiFlags == UiFlags.PIPELINE_IN) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Default.ArrowRight,
                    contentDescription = null
                )
            }
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "填充"
            )
        }

        ElevatedButton(
            enabled = uiState.uiFlags == UiFlags.NONE || uiState.uiFlags == UiFlags.PIPELINE_OUT,
            onClick = {
                scope.launch {
                    if (uiState.jobState.status != JobState.RUNNING) {
                        if (uiState.uiFlags == UiFlags.NONE) {
                            uiEvent(HomeUiEvent.Pipeline(3))
                        } else if (uiState.uiFlags == UiFlags.PIPELINE_OUT) {
                            uiEvent(HomeUiEvent.Pipeline(0))
                        }
                    }
                }
            }) {
            if (uiState.uiFlags == UiFlags.PIPELINE_OUT) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Default.ArrowLeft,
                    contentDescription = null
                )
            }
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "回吸"
            )
        }
    }
}

@Composable
fun ProgramList(
    entities: LazyPagingItems<Program>,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit = {},
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
            ProgramItem(
                index = index,
                item = item,
                selected = uiState.selected == item.id
            ) { double ->
                scope.launch {
                    if (!double) {
                        uiEvent(HomeUiEvent.ToggleSelected(item.id))
                        uiEvent(HomeUiEvent.NavTo(PageType.HOME))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobContent(
    entities: List<Program>,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit
) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
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

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.5f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small,
                        )
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
                        }
                        .padding(horizontal = 32.dp, vertical = 4.dp)
                ) {
                    val selected = entities.find { it.id == uiState.selected } ?: Program()
                    Text(
                        text = selected.displayText,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            fontStyle = FontStyle.Italic,
                        )
                    )
                    Text(
                        text = selected.createTime.dateFormat("yyyy/MM/dd"),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                        ),
                        color = Color.Gray,
                    )
                }
                FlowRow {
                    info.forEach {
                        Text(
                            modifier = Modifier
                                .padding(4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

            }
            OrificePlate(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(328.dp)
                    .padding(16.dp),
                row = uiState.jobState.orificePlate.row,
                column = uiState.jobState.orificePlate.column,
                selected = uiState.jobState.finished
            )
        }

        JobActionCard(
            modifier = Modifier.fillMaxWidth(1f),
            uiState = uiState,
            uiEvent = uiEvent
        )
    }
}