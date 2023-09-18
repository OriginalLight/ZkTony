package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.R
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.HomeAppBar
import com.zktony.android.ui.components.ProgramItem
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.*
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.format
import com.zktony.android.utils.extra.timeFormat
import kotlinx.coroutines.delay
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
        HomeAppBar(uiState = uiState) { navigation() }

        AnimatedVisibility(visible = uiState.job == null) {
            HomeActions(uiState, uiEvent)
        }

        AnimatedContent(targetState = uiState.page) {
            when (uiState.page) {
                PageType.PROGRAM_LIST -> ProgramList(entities, uiState, uiEvent)
                PageType.HOME -> JobContent(entities.toList(), uiState, uiEvent)
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
            onClick = { uiEvent(HomeUiEvent.Reset) }) {
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
            onClick = { uiEvent(HomeUiEvent.Clean) }) {
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
            enabled = uiState.uiFlags == UiFlags.NONE || uiState.uiFlags == UiFlags.SYRINGE_IN,
            onClick = {
                scope.launch {
                    if (uiState.uiFlags == UiFlags.NONE) {
                        uiEvent(HomeUiEvent.Syringe(1))
                    } else if (uiState.uiFlags == UiFlags.SYRINGE_IN) {
                        uiEvent(HomeUiEvent.Syringe(0))
                    }
                }
            }) {
            if (uiState.uiFlags == UiFlags.SYRINGE_IN) {
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
                text = "填充/促凝剂"
            )
        }

        ElevatedButton(
            enabled = uiState.uiFlags == UiFlags.NONE || uiState.uiFlags == UiFlags.SYRINGE_OUT,
            onClick = {
                scope.launch {
                    if (uiState.uiFlags == UiFlags.NONE) {
                        uiEvent(HomeUiEvent.Syringe(2))
                    } else if (uiState.uiFlags == UiFlags.SYRINGE_OUT) {
                        uiEvent(HomeUiEvent.Syringe(0))
                    }
                }
            }) {
            if (uiState.uiFlags == UiFlags.SYRINGE_OUT) {
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
                text = "回吸/促凝剂"
            )
        }

        ElevatedButton(
            enabled = uiState.uiFlags == UiFlags.NONE || uiState.uiFlags == UiFlags.PIPELINE_IN,
            onClick = {
                scope.launch {
                    if (uiState.uiFlags == UiFlags.NONE) {
                        uiEvent(HomeUiEvent.Pipeline(1))
                    } else if (uiState.uiFlags == UiFlags.PIPELINE_IN) {
                        uiEvent(HomeUiEvent.Pipeline(0))
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
                text = "填充/胶体"
            )
        }

        ElevatedButton(
            enabled = uiState.uiFlags == UiFlags.NONE || uiState.uiFlags == UiFlags.PIPELINE_OUT,
            onClick = {
                scope.launch {
                    if (uiState.uiFlags == UiFlags.NONE) {
                        uiEvent(HomeUiEvent.Pipeline(2))
                    } else if (uiState.uiFlags == UiFlags.PIPELINE_OUT) {
                        uiEvent(HomeUiEvent.Pipeline(0))
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
                text = "回吸/胶体"
            )
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
        modifier = Modifier.padding(16.dp),
        contentPadding = PaddingValues(16.dp),
        columns = GridCells.Fixed(3),
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobContent(
    entities: List<Program>,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    var time by remember { mutableLongStateOf(0L) }
    val item = entities.find { it.id == uiState.selected } ?: Program()

    LaunchedEffect(key1 = uiState.selected) {
        if (uiState.selected == 0L && entities.isNotEmpty()) {
            uiEvent(HomeUiEvent.ToggleSelected(entities[0].id))
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
        modifier = Modifier.fillMaxSize(),
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
                                text = item.displayText,
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
                            if (uiState.uiFlags == UiFlags.NONE) {
                                if (uiState.selected == 0L) {
                                    navigationActions.navigate(Route.PROGRAM)
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
                        if (uiState.uiFlags == UiFlags.NONE) {
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