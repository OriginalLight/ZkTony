package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.zktony.android.ui.components.Header
import com.zktony.android.ui.components.OrificePlate
import com.zktony.android.ui.components.OrificePlateCard
import com.zktony.android.ui.components.RuntimeCard
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.NavigationType
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.RuntimeState
import com.zktony.android.utils.RuntimeStatus
import com.zktony.android.utils.ext.dateFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun Home(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    toggleDrawer: (NavigationType) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        when (uiState.page) {
            PageType.START -> viewModel.uiEvent(HomeUiEvent.NavTo(PageType.LIST))
            else -> {}
        }
    }

    AnimatedVisibility(visible = uiState.page == PageType.LIST) {
        MenuContent(
            modifier = modifier,
            uiState = uiState,
            uiEvent = viewModel::uiEvent,
            navController = navController,
        )
    }

    AnimatedVisibility(visible = uiState.page == PageType.START) {
        StartContent(
            modifier = modifier,
            uiState = uiState,
            uiEvent = viewModel::uiEvent,
            toggleDrawer = toggleDrawer,
        )
    }

    AnimatedVisibility(visible = uiState.page == PageType.RUNTIME) {
        RuntimeContent(
            modifier = modifier,
            uiState = uiState.runtime,
            uiEvent = viewModel::uiEvent,
            toggleDrawer = toggleDrawer,
        )
    }
}

@Composable
fun MenuContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit = {},
    navController: NavHostController,
) {
    var pipeline by remember { mutableStateOf(0) }
    var time by remember { mutableStateOf(0) }
    var cleanTime by remember { mutableStateOf(30) }

    LaunchedEffect(key1 = uiState.loading) {
        while (true) {
            if (uiState.loading != 0) {
                time += 1
            } else {
                time = 0
            }
            if (uiState.loading == 2 && time >= cleanTime) {
                uiEvent(HomeUiEvent.Pipeline(0))
            }
            delay(1000L)
        }
    }

    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(32.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        item {
            FunctionCard(
                title = "复位",
                description = "依次复位X轴、Y轴",
                image = {
                    if (uiState.loading == 1) {
                        // Display loading indicator
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(72.dp),
                                strokeWidth = 8.dp,
                            )

                            Text(
                                modifier = Modifier.align(Alignment.BottomEnd),
                                text = "${time}s",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = FontFamily.Monospace,
                                fontStyle = FontStyle.Italic,
                            )
                        }
                    } else {
                        // Display reset icon
                        Image(
                            modifier = Modifier.size(96.dp),
                            painter = painterResource(id = R.drawable.ic_reset),
                            contentDescription = null,
                        )
                    }
                })
            {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.loading == 0,
                    onClick = { uiEvent(HomeUiEvent.Reset) }
                ) {
                    Text(
                        text = "开始复位",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        item {
            FunctionCard(
                title = "管路清洗",
                description = "清洗管路残留试剂",
                image = {
                    if (uiState.loading == 2) {
                        // Display loading indicator
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(72.dp),
                                strokeWidth = 8.dp,
                            )
                            Text(
                                modifier = Modifier.align(Alignment.BottomEnd),
                                text = "${cleanTime - time}s",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = FontFamily.Monospace,
                                fontStyle = FontStyle.Italic,
                            )
                        }
                    } else {
                        // Display reset icon
                        Image(
                            modifier = Modifier.size(96.dp),
                            painter = painterResource(id = R.drawable.ic_water),
                            contentDescription = null,
                        )
                    }
                })
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (uiState.loading == 2) {
                                uiEvent(HomeUiEvent.Pipeline(0))
                            } else {
                                cleanTime += 10
                            }
                        }
                    ) {
                        if (uiState.loading == 2) {
                            Text(
                                text = "取消",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        } else {
                            Text(
                                text = "${cleanTime}s",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                                fontStyle = FontStyle.Italic,
                            )
                        }
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = uiState.loading == 0,
                        onClick = { uiEvent(HomeUiEvent.Pipeline(0)) }
                    ) {
                        Text(
                            text = "开始",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
        item {
            FunctionCard(
                title = if (pipeline == 0) "填充" else "回吸",
                description = "填充/回吸，点击按钮切换",
                image = {
                    if (uiState.loading == 3) {
                        // Display loading indicator
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(72.dp),
                                strokeWidth = 8.dp,
                            )
                            Text(
                                modifier = Modifier.align(Alignment.BottomEnd),
                                text = "${time}s",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = FontFamily.Monospace,
                                fontStyle = FontStyle.Italic,
                            )
                        }
                    } else {
                        // Display reset icon
                        Image(
                            modifier = Modifier.size(96.dp),
                            painter = painterResource(id = R.drawable.ic_pipeline),
                            contentDescription = null,
                        )
                    }
                })
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (uiState.loading == 3) {
                                uiEvent(HomeUiEvent.Pipeline(0))
                            } else {
                                pipeline = if (pipeline == 0) 1 else 0
                            }
                        }
                    ) {
                        if (uiState.loading == 3) {
                            Text(
                                text = "取消",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                            )
                        }
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = uiState.loading == 0,
                        onClick = { uiEvent(HomeUiEvent.Pipeline(pipeline + 1)) }
                    ) {
                        Text(
                            text = "开始",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
        item {
            FunctionCard(
                title = "程序运行",
                description = "选择并执行分液程序",
                image = {
                    Image(
                        modifier = Modifier.size(96.dp),
                        painter = painterResource(id = R.drawable.ic_start),
                        contentDescription = null,
                    )
                })
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = uiState.loading == 0,
                        onClick = {
                            if (uiState.loading == 0) {
                                if (uiState.entities.isEmpty()) {
                                    navController.navigate(Route.PROGRAM)
                                } else {
                                    uiEvent(HomeUiEvent.NavTo(PageType.START))
                                }
                            }
                        }
                    ) {
                        Text(
                            text = "开始 ✔",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit = {},
    toggleDrawer: (NavigationType) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Header(
            onBackPressed = { uiEvent(HomeUiEvent.NavTo(PageType.LIST)) },
        ) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { active = false },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("搜索") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val items =
                        uiState.entities.filter { query.isNotEmpty() && it.text.contains(query) }
                    items(items.size) {
                        val item = items[it]
                        ListItem(
                            headlineContent = { Text(item.text) },
                            supportingContent = { Text(item.createTime.dateFormat("yyyy/MM/dd")) },
                            leadingContent = {
                                if (item.text == query) Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.clickable {
                                query = item.text
                                active = false
                            }
                        )
                    }
                }
            }
        }

        LazyVerticalGrid(
            modifier = modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = MaterialTheme.shapes.medium
                ),
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val items = uiState.entities.filter { it.text.contains(query) }

            itemsIndexed(items = items) { index, item ->
                Card(
                    onClick = {
                        scope.launch {
                            uiEvent(HomeUiEvent.ToggleSelected(item.id))
                            uiEvent(HomeUiEvent.NavTo(PageType.RUNTIME))
                            toggleDrawer(NavigationType.NONE)
                        }
                    },
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Display the entity image and title
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
}

@Composable
fun RuntimeContent(
    modifier: Modifier = Modifier,
    uiState: RuntimeState,
    uiEvent: (HomeUiEvent) -> Unit = {},
    toggleDrawer: (NavigationType) -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        FloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd),
            onClick = {
                scope.launch {
                    if (uiState.status == RuntimeStatus.STOPPED) {
                        uiEvent(HomeUiEvent.NavTo(PageType.LIST))
                        toggleDrawer(NavigationType.NAVIGATION_RAIL)
                    }
                }
            }
        ) {
            if (uiState.status == RuntimeStatus.STOPPED) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null
                )
            }
        }

        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OrificePlate(
                    modifier = Modifier.fillMaxHeight(0.5f),
                    row = uiState.orificePlate.row,
                    column = uiState.orificePlate.column,
                    selected = uiState.selected,
                )

                OrificePlateCard(orificePlate = uiState.orificePlate)
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RuntimeCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f),
                    status = uiState.status,
                    process = uiState.process,
                ) {
                    scope.launch {
                        uiEvent(HomeUiEvent.Runtime(it))
                    }
                }
            }
        }
    }
}

@Composable
fun FunctionCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    image: @Composable () -> Unit,
    button: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Blue.copy(alpha = 0.1f),
                                Color.Cyan.copy(alpha = 0.3f),
                                Color.Blue.copy(alpha = 0.3f),
                            )
                        ),
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                image()
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        text = description,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Serif,
                    )
                }
            }
            button()

        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun MenuContentPreview() {
    MenuContent(
        uiState = HomeUiState(),
        navController = rememberNavController()
    )
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun StartContentPreview() {
    val entities = listOf(
        Program(),
        Program(),
    )
    StartContent(uiState = HomeUiState(entities = entities))
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun RuntimeContentPreview() {
    RuntimeContent(uiState = RuntimeState())
}