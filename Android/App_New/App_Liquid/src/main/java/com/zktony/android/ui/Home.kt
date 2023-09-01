package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.*
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.extra.dateFormat
import kotlinx.coroutines.launch

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: HomeViewModel,
    navigationActions: NavigationActions,
    snackbarHostState: SnackbarHostState,
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
        if (message != null) {
            snackbarHostState.showSnackbar(
                message = message ?: "未知错误",
                actionLabel = "关闭",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            HomeAppBar(
                enable = uiState.jobState.status == JobStatus.STOPPED,
                navigationActions = navigationActions
            ) {
                AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_LIST) {
                    ElevatedButton(onClick = navigation) {
                        Icon(
                            imageVector = Icons.Default.Reply,
                            contentDescription = null
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
    ) { paddingValues ->
        HomeScreen(
            modifier = modifier.padding(paddingValues),
            navController = navController,
            uiState = uiState,
            uiEvent = viewModel::uiEvent
        )
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit
) {

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(visible = uiState.jobState.status == JobStatus.STOPPED) {
            HomeActions(uiState, uiEvent)
        }

        AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_LIST) {
            ProgramList(uiState, uiEvent)
        }

        AnimatedVisibility(visible = uiState.page == PageType.HOME) {
            JobContent(uiState, uiEvent, navController)
        }
    }

}

@Composable
fun HomeActions(
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = uiState.entities) {
        if (uiState.entities.isEmpty()) {
            uiEvent(HomeUiEvent.ToggleSelected(0L))
        } else {
            if (uiState.selected == 0L) {
                uiEvent(HomeUiEvent.ToggleSelected(uiState.entities.getOrNull(0)?.id ?: 0L))
            } else {
                if (!uiState.entities.any { it.id == uiState.selected }) {
                    uiEvent(HomeUiEvent.ToggleSelected(uiState.entities.getOrNull(0)?.id ?: 0L))
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            )
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ElevatedButton(
            enabled = uiState.loading == 0 || uiState.loading == 1,
            onClick = {
                scope.launch {
                    if (uiState.loading == 0 || uiState.loading == 1) {
                        uiEvent(HomeUiEvent.Reset)
                    }
                }
            }) {
            if (uiState.loading == 1) {
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
            enabled = uiState.loading == 0 || uiState.loading == 2,
            onClick = {
                scope.launch {
                    if (uiState.loading == 0) {
                        uiEvent(HomeUiEvent.Pipeline(1))
                    } else if (uiState.loading == 2) {
                        uiEvent(HomeUiEvent.Pipeline(0))
                    }
                }
            }) {
            if (uiState.loading == 2) {
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
            enabled = uiState.loading == 0 || uiState.loading == 3,
            onClick = {
                scope.launch {
                    if (uiState.loading == 0) {
                        uiEvent(HomeUiEvent.Pipeline(2))
                    } else if (uiState.loading == 3) {
                        uiEvent(HomeUiEvent.Pipeline(0))
                    }
                }
            }) {
            if (uiState.loading == 3) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = null
                )
            }
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "填充"
            )
        }

        ElevatedButton(
            enabled = uiState.loading == 0 || uiState.loading == 4,
            onClick = {
                scope.launch {
                    if (uiState.loading == 0) {
                        uiEvent(HomeUiEvent.Pipeline(3))
                    } else if (uiState.loading == 4) {
                        uiEvent(HomeUiEvent.Pipeline(0))
                    }
                }
            }) {
            if (uiState.loading == 4) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Default.ArrowBackIos,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramList(
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.small
            ),
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        itemsIndexed(items = uiState.entities) { index, item ->
            Card(
                onClick = {
                    scope.launch {
                        uiEvent(HomeUiEvent.ToggleSelected(item.id))
                        uiEvent(HomeUiEvent.NavTo(PageType.HOME))
                    }
                },
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.height(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "${index + 1}、",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = item.text,
                        fontSize = 20.sp,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = item.createTime.dateFormat("yyyy/MM/dd"),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

@Composable
fun JobContent(
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit,
    navController: NavHostController
) {

    val scope = rememberCoroutineScope()
    var showInfo by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val height = if (uiState.jobState.status == JobStatus.STOPPED) 0.7f else 0.6f

        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight(height),
            contentAlignment = Alignment.Center
        ) {
            if (showInfo) {
                OrificePlateCard(
                    modifier = Modifier.fillMaxSize(),
                    orificePlate = uiState.jobState.orificePlate
                )
            } else {
                OrificePlate(
                    modifier = Modifier.fillMaxSize(),
                    row = uiState.jobState.orificePlate.row,
                    column = uiState.jobState.orificePlate.column,
                    selected = uiState.jobState.finished
                )
            }

            Row(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = 0.dp.roundToPx(),
                            y = (-64).dp.roundToPx(),
                        )
                    }
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                    )
                    .align(Alignment.TopEnd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    modifier = Modifier.size(48.dp),
                    onClick = { showInfo = !showInfo },
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = 0.dp.roundToPx(),
                            y = (-64).dp.roundToPx(),
                        )
                    }
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                    )
                    .clip(MaterialTheme.shapes.small)
                    .align(Alignment.TopStart)
                    .clickable {
                        scope.launch {
                            if (uiState.jobState.status == JobStatus.STOPPED) {
                                if (uiState.entities.isNotEmpty()) {
                                    uiEvent(HomeUiEvent.NavTo(PageType.PROGRAM_LIST))
                                } else {
                                    navController.navigate(Route.Program)
                                }
                            }
                        }
                    }
                    .padding(horizontal = 32.dp, vertical = 4.dp)
            ) {
                val selected = uiState.entities.find { it.id == uiState.selected } ?: Program()
                Text(
                    text = selected.text,
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

        }

        JobActionCard(
            modifier = Modifier.fillMaxWidth(1f),
            uiState = uiState,
            uiEvent = uiEvent
        )
    }
}