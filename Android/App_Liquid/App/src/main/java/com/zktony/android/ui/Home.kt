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
import com.zktony.android.data.entities.internal.OrificePlate
import com.zktony.android.ui.components.CountDownDialog
import com.zktony.android.ui.components.FinishDialog
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeRoute(viewModel: HomeViewModel) {

    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    val page by viewModel.page.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val uiFlags by viewModel.uiFlags.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val orificePlate by viewModel.orificePlate.collectAsStateWithLifecycle()
    val finished by viewModel.finished.collectAsStateWithLifecycle()

    val entities = viewModel.entities.collectAsLazyPagingItems()
    val navigation: () -> Unit = {
        scope.launch {
            when (page) {
                PageType.PROGRAM_LIST -> viewModel.dispatch(HomeIntent.NavTo(PageType.HOME))
                else -> {}
            }
        }
    }

    BackHandler { navigation() }

    LaunchedEffect(key1 = uiFlags) {
        if (uiFlags is UiFlags.Message) {
            snackbarHostState.showSnackbar((uiFlags as UiFlags.Message).message)
            viewModel.dispatch(HomeIntent.Flags(UiFlags.none()))
        }
    }

    Column {
        HomeAppBar(page) { navigation() }
        AnimatedContent(targetState = page) {
            when (page) {
                PageType.PROGRAM_LIST -> ProgramList(entities, viewModel::dispatch)
                PageType.HOME -> HomeContent(
                    entities.toList(),
                    selected,
                    uiFlags,
                    status,
                    orificePlate,
                    finished,
                    viewModel::dispatch
                )

                else -> {}
            }
        }
    }
}

@Composable
fun ProgramList(
    entities: LazyPagingItems<Program>,
    dispatch: (HomeIntent) -> Unit,
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
                            dispatch(HomeIntent.Selected(item.id))
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeContent(
    entities: List<Program>,
    selected: Long,
    uiFlags: UiFlags,
    status: Int,
    orificePlate: OrificePlate,
    finished: List<Triple<Int, Int, Color>>,
    dispatch: (HomeIntent) -> Unit
) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current
    var dialog by remember { mutableStateOf(false) }
    val tankAbscissa by rememberDataSaverState(Constants.ZT_0003, 0.0)
    val tankOrdinate by rememberDataSaverState(Constants.ZT_0004, 0.0)

    LaunchedEffect(key1 = entities) {
        if (entities.isEmpty()) {
            dispatch(HomeIntent.Selected(0L))
        }
    }

    if (dialog) {
        CountDownDialog(
            onStart = {
                scope.launch {
                    dispatch(HomeIntent.Pipeline(1))
                }
            },
            onStop = {
                scope.launch {
                    dispatch(HomeIntent.Pipeline(0))
                }
            },
            onCancel = {
                dialog = false
            }
        )
    }

    if (uiFlags is UiFlags.Objects && uiFlags.objects == 4) {
        FinishDialog { code ->
            scope.launch {
                when (code) {
                    0 -> {
                        dispatch(HomeIntent.Flags(UiFlags.none()))
                    }

                    1 -> {
                        dispatch(HomeIntent.Reset)
                    }

                    else -> {
                        scope.launch {
                            start {
                                with(index = 0, pdv = tankAbscissa)
                                with(index = 1, pdv = tankOrdinate)
                            }
                        }
                        dispatch(HomeIntent.Flags(UiFlags.none()))
                    }
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val program = entities.firstOrNull { it.id == selected } ?: Program()

            ListItem(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        scope.launch {
                            if (status == 0) {
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
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = program.displayText,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        orificePlate.getInfo().forEach {
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

            if (selected != 0L) {
                OrificePlate(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(328.dp),
                    row = orificePlate.row,
                    column = orificePlate.column,
                    selected = finished
                )
            }
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
                                if (uiFlags is UiFlags.None && selected != 0L) {
                                    when (status) {
                                        0 -> { dispatch(HomeIntent.Start) }
                                        1 -> { dispatch(HomeIntent.Pause) }
                                        2 -> { dispatch(HomeIntent.Resume) }
                                    }
                                } else {
                                    snackbarHostState.showSnackbar("请中止其他操作或选择程序")
                                }
                            }
                        },
                    imageVector = if (status == 1) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = when (status) {
                        0 -> Color.Blue
                        1 -> Color.DarkGray
                        2 -> Color.Yellow
                        else -> Color.Gray
                    }
                )

                if (status != 0) {
                    Icon(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .clickable {
                                scope.launch {
                                    dispatch(HomeIntent.Stop)
                                    snackbarHostState.showSnackbar("程序中止")
                                }
                            },
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.Red
                    )
                }
            }
            if (status != 1) {
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
                                    if (uiFlags is UiFlags.None) {
                                        dispatch(HomeIntent.Reset)
                                    }
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 1) "复位中" else "复位",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 1) Color.Red else Color.Unspecified
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
                                    if (uiFlags is UiFlags.None) {
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
                                    if (uiFlags is UiFlags.None || (uiFlags is UiFlags.Objects && uiFlags.objects == 2)) {
                                        if (uiFlags is UiFlags.None) {
                                            dispatch(HomeIntent.Pipeline(2))
                                        } else {
                                            dispatch(HomeIntent.Pipeline(0))
                                        }
                                    }
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 2) "填充中" else "填充",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 2) Color.Red else Color.Unspecified
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
                                    if (uiFlags is UiFlags.None || (uiFlags is UiFlags.Objects && uiFlags.objects == 3)) {
                                        if (uiFlags is UiFlags.None) {
                                            dispatch(HomeIntent.Pipeline(3))
                                        } else {
                                            dispatch(HomeIntent.Pipeline(0))
                                        }
                                    }
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        text = if (uiFlags is UiFlags.Objects && uiFlags.objects == 3) "回吸中" else "回吸",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uiFlags is UiFlags.Objects && uiFlags.objects == 3) Color.Red else Color.Unspecified
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