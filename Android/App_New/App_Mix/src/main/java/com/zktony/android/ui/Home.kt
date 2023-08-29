package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.zktony.android.R
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.HomeAppBar
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.format
import com.zktony.android.utils.extra.timeFormat
import kotlinx.coroutines.delay
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
                enable = uiState.job == null,
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

        AnimatedVisibility(visible = uiState.job == null) {
            HomeActions(uiState = uiState, uiEvent = uiEvent)
        }

        AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_LIST) {
            ProgramList(uiState = uiState, uiEvent = uiEvent)
        }

        AnimatedVisibility(visible = uiState.page == PageType.HOME) {
            JobContent(uiState = uiState, uiEvent = uiEvent, navController = navController)
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
                    if (uiState.loading == 0 || uiState.loading == 2) {
                        uiEvent(HomeUiEvent.Clean)
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
                        uiEvent(HomeUiEvent.Syringe(1))
                    } else if (uiState.loading == 3) {
                        uiEvent(HomeUiEvent.Syringe(0))
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
                text = "填充/促凝剂"
            )
        }

        ElevatedButton(
            enabled = uiState.loading == 0 || uiState.loading == 4,
            onClick = {
                scope.launch {
                    if (uiState.loading == 0) {
                        uiEvent(HomeUiEvent.Syringe(2))
                    } else if (uiState.loading == 4) {
                        uiEvent(HomeUiEvent.Syringe(0))
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
                text = "回吸/促凝剂"
            )
        }

        ElevatedButton(
            enabled = uiState.loading == 0 || uiState.loading == 5,
            onClick = {
                scope.launch {
                    if (uiState.loading == 0) {
                        uiEvent(HomeUiEvent.Pipeline(1))
                    } else if (uiState.loading == 5) {
                        uiEvent(HomeUiEvent.Pipeline(0))
                    }
                }
            }) {
            if (uiState.loading == 5) {
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
                text = "填充/胶体"
            )
        }

        ElevatedButton(
            enabled = uiState.loading == 0 || uiState.loading == 6,
            onClick = {
                scope.launch {
                    if (uiState.loading == 0) {
                        uiEvent(HomeUiEvent.Pipeline(2))
                    } else if (uiState.loading == 6) {
                        uiEvent(HomeUiEvent.Pipeline(0))
                    }
                }
            }) {
            if (uiState.loading == 6) {
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
                text = "回吸/胶体"
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramList(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit = {},
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
    var time by remember { mutableLongStateOf(0L) }
    val item = uiState.entities.find { it.id == uiState.selected } ?: Program()

    LaunchedEffect(key1 = uiState.selected) {
        if (uiState.selected == 0L && uiState.entities.isNotEmpty()) {
            uiEvent(HomeUiEvent.ToggleSelected(uiState.entities[0].id))
        }
    }

    LaunchedEffect(key1 = uiState.job) {
        while (true) {
            if (uiState.job != null) {
                time += 1
            } else {
                time = 0
            }
            delay(1000L)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Card(
                    onClick = {
                        scope.launch {
                            if (uiState.job == null) {
                                uiEvent(HomeUiEvent.NavTo(PageType.PROGRAM_LIST))
                            }
                        }
                    },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = item.text,
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    fontStyle = FontStyle.Italic,
                                )
                            )
                            Text(
                                text = item.createTime.dateFormat("yyyy/MM/dd"),
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                ),
                                color = Color.Gray,
                            )
                        }
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Default.ArrowRight,
                            contentDescription = null
                        )
                    }
                }

                Card {
                    Box(
                        modifier = Modifier.padding(8.dp),
                    ) {
                        if (uiState.job != null) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.TopStart),
                                strokeWidth = 4.dp,
                            )
                        }
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = time.timeFormat(),
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Card {
                        Text(
                            modifier = Modifier
                                .padding(16.dp),
                            text = stringResource(id = R.string.glue_making),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            text = item.dosage.coagulant.format(1),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                        )
                    }


                    Card(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            text = item.dosage.colloid.format(1),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Card {
                        Text(
                            modifier = Modifier
                                .padding(16.dp),
                            text = stringResource(id = R.string.pre_drain),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            text = item.dosage.preCoagulant.format(1),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                        )
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            text = item.dosage.preColloid.format(1),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ElevatedCard(
                    onClick = {
                        if (uiState.job == null) {
                            if (uiState.loading == 0) {
                                if (uiState.selected == 0L) {
                                    navController.navigate(Route.PROGRAM)
                                } else {
                                    uiEvent(HomeUiEvent.Start)
                                }
                            }
                        } else {
                            uiEvent(HomeUiEvent.Stop)
                        }
                    },
                ) {
                    if (uiState.job == null) {
                        if (uiState.loading == 0) {
                            Icon(
                                modifier = Modifier.size(196.dp),
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(196.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(156.dp),
                                    strokeWidth = 8.dp,
                                    color = Color.Green,
                                )
                            }
                        }
                    } else {
                        Icon(
                            modifier = Modifier.size(196.dp),
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun RuntimeContentPreview() {
    val entities = listOf(
        Program(id = 1),
        Program(),
    )
    JobContent(
        uiState = HomeUiState(entities = entities, selected = 1L),
        uiEvent = {},
        navController = rememberNavController()
    )
}