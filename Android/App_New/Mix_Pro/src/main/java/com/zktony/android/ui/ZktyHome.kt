package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.zktony.android.R
import com.zktony.android.logic.data.entities.ProgramEntity
import com.zktony.android.ui.components.ZktyTopAppBar
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.NavigationType
import com.zktony.android.ui.utils.PageType
import com.zktony.core.ext.format
import com.zktony.core.ext.getTimeFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Composable
fun ZktyHome(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    toggleDrawer: (NavigationType) -> Unit = {},
    viewModel: ZktyHomeViewModel = koinViewModel(),
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        when (uiState.page) {
            PageType.LIST -> {}
            else -> viewModel.event(HomeEvent.NavTo(PageType.LIST))
        }
    }

    Column(modifier = modifier) {
        AnimatedVisibility(visible = uiState.page == PageType.START) {
            ZktyTopAppBar(
                title = stringResource(id = R.string.tab_program),
                navigation = { viewModel.event(HomeEvent.NavTo(PageType.LIST)) }
            )
        }
        // list
        AnimatedVisibility(visible = uiState.page == PageType.LIST) {
            HomeList(
                modifier = Modifier,
                uiState = uiState,
                event = viewModel::event,
                navController = navController,
            )
        }
        // start
        AnimatedVisibility(visible = uiState.page == PageType.START) {
            HomeStart(
                modifier = Modifier,
                uiState = uiState,
                event = viewModel::event,
                toggleDrawer = toggleDrawer,
            )
        }
        // Runtime
        AnimatedVisibility(visible = uiState.page == PageType.RUNTIME) {
            HomeRuntime(
                modifier = Modifier,
                uiState = uiState,
                event = viewModel::event,
                toggleDrawer = toggleDrawer,
            )
        }
    }
}


@Composable
fun HomeList(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    event: (HomeEvent) -> Unit = {},
    navController: NavHostController,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium
            ),
        verticalArrangement = Arrangement.Center,
    ) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ListItem(title = "复 位") {
                    Image(
                        modifier = Modifier.size(64.dp),
                        painter = painterResource(id = R.drawable.ic_reset),
                        contentDescription = null,
                    )
                }
            }
            item {
                ListItem(title = "填充-促凝剂") {
                    Box {
                        Image(
                            modifier = Modifier.size(64.dp),
                            painter = painterResource(id = R.drawable.ic_syringe),
                            contentDescription = null,
                        )
                        Icon(
                            modifier = Modifier.align(Alignment.BottomEnd),
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                        )
                    }
                }
            }
            item {
                ListItem(title = "回吸-促凝剂") {
                    Box {
                        Image(
                            modifier = Modifier.size(64.dp),
                            painter = painterResource(id = R.drawable.ic_syringe),
                            contentDescription = null,
                        )
                        Icon(
                            modifier = Modifier.align(Alignment.BottomEnd),
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                        )
                    }
                }
            }
            item {
                ListItem(title = "填充-胶体") {
                    Box {
                        Image(
                            modifier = Modifier.size(64.dp),
                            painter = painterResource(id = R.drawable.ic_pipeline),
                            contentDescription = null,
                        )
                        Icon(
                            modifier = Modifier.align(Alignment.TopEnd),
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                        )
                    }
                }
            }
            item {
                ListItem(title = "回吸-胶体") {
                    Box {
                        Image(
                            modifier = Modifier.size(64.dp),
                            painter = painterResource(id = R.drawable.ic_pipeline),
                            contentDescription = null,
                        )
                        Icon(
                            modifier = Modifier.align(Alignment.TopEnd),
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                        )
                    }
                }
            }
        }

        ListItem(
            modifier = Modifier.padding(horizontal = 196.dp),
            title = "开始程序",
            onClick = {
                if (uiState.entities.isEmpty()) {
                    navController.navigate(Route.PROGRAM)
                } else {
                    event(HomeEvent.NavTo(PageType.START))
                }
            }
        ) {
            Image(
                modifier = Modifier.size(64.dp),
                painter = painterResource(id = R.drawable.ic_start),
                contentDescription = null,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit = { },
    colors: CardColors = CardDefaults.elevatedCardColors(),
    image: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = modifier,
        onClick = onClick,
        colors = colors,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            image()
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeStart(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    event: (HomeEvent) -> Unit = {},
    toggleDrawer: (NavigationType) -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium
            ),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            uiState.entities.forEach {
                item {
                    Card(
                        onClick = {
                            scope.launch {
                                event(HomeEvent.ToggleSelected(it.id))
                                event(HomeEvent.NavTo(PageType.RUNTIME))
                                toggleDrawer(NavigationType.NONE)
                            }
                        },
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row {
                                Image(
                                    modifier = Modifier.size(32.dp),
                                    painter = painterResource(id = R.drawable.ic_program),
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = it.text,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }

                            Column {
                                Text(
                                    text = "${it.volume[0].format(1)} μL - ${it.volume[1].format(1)} μL",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                )
                                Text(
                                    text = "${it.volume[2].format(1)} μL - ${it.volume[3].format(1)} μL",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRuntime(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    event: (HomeEvent) -> Unit = {},
    toggleDrawer: (NavigationType) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var time by remember { mutableLongStateOf(0L) }
    val item = uiState.entities.find { it.id == uiState.selected }!!

    LaunchedEffect(key1 = uiState.job) {
        while (true) {
            if (uiState.job != null) {
                time += 1
            } else {
                time = 0
            }
            delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center,
    ) {
        FloatingActionButton(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopEnd),
            onClick = {
                scope.launch {
                    if (uiState.job == null) {
                        event(HomeEvent.NavTo(PageType.LIST))
                        toggleDrawer(NavigationType.PERMANENT_NAVIGATION_DRAWER)
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null
            )
        }

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
                Card {
                    Row(
                        modifier = Modifier.padding(8.dp),
                    ) {
                        AnimatedVisibility(visible = uiState.job != null) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 4.dp,
                            )
                        }
                        Text(
                            modifier = Modifier.weight(1f),
                            text = time.getTimeFormat(),
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
                            text = item.volume[0].format(1),
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
                            text = item.volume[1].format(1),
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
                            text = item.volume[2].format(1),
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
                            text = item.volume[3].format(1),
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
                    onClick = { event(HomeEvent.Start) },
                ) {
                    if (uiState.job == null) {
                        Image(
                            modifier = Modifier.size(196.dp),
                            painter = painterResource(id = R.drawable.ic_start),
                            contentDescription = null
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(196.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(156.dp),
                                strokeWidth = 8.dp,
                                color = Color.Red,
                            )
                            Image(
                                modifier = Modifier.size(96.dp),
                                painter = painterResource(id = R.drawable.ic_stop),
                                contentDescription = null
                            )
                        }
                    }

                }
            }
        }
    }
}


@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun HomeMenuPreview() {
    HomeList(
        uiState = HomeUiState(),
        navController = rememberNavController()
    )
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun HomeStartPreview() {
    val entities = listOf(
        ProgramEntity(),
        ProgramEntity(),
    )
    HomeStart(uiState = HomeUiState(entities = entities))
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun HomeRuntimePreview() {
    val entities = listOf(
        ProgramEntity(id = 1),
        ProgramEntity(),
    )
    HomeRuntime(uiState = HomeUiState(entities = entities, selected = 1L))
}