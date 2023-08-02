package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.zktony.android.data.model.Program
import com.zktony.android.ui.components.MyTopAppBar
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.NavigationType
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.ext.dateFormat
import com.zktony.android.utils.ext.format
import com.zktony.android.utils.ext.timeFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


/**
 * Composable function for the Home screen.
 *
 * @param modifier The modifier to apply to the composable.
 * @param navController The NavHostController used for navigation.
 * @param toggleDrawer The function to toggle the drawer.
 * @param viewModel The HomeViewModel used to manage the UI state.
 */
@Composable
fun Home(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    toggleDrawer: (NavigationType) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {

    // Observe the UI state from the view model
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle the back button press
    BackHandler {
        when (uiState.page) {
            PageType.START -> viewModel.event(HomeEvent.NavTo(PageType.LIST))
            else -> {}
        }
    }

    // List content
    AnimatedVisibility(visible = uiState.page == PageType.LIST) {
        MenuContent(
            modifier = modifier,
            uiState = uiState,
            event = viewModel::event,
            navController = navController,
        )
    }
    // Start content
    AnimatedVisibility(visible = uiState.page == PageType.START) {
        StartContent(
            modifier = modifier,
            uiState = uiState,
            event = viewModel::event,
            toggleDrawer = toggleDrawer,
        )
    }
    // Runtime content
    AnimatedVisibility(visible = uiState.page == PageType.RUNTIME) {
        RuntimeContent(
            modifier = modifier,
            uiState = uiState,
            event = viewModel::event,
            toggleDrawer = toggleDrawer,
        )
    }
}

/**
 * Composable function for the list content of the Home screen.
 *
 * @param modifier The modifier to apply to the composable.
 * @param uiState The HomeUiState used to manage the UI state.
 * @param event The function to handle events.
 * @param navController The NavHostController used for navigation.
 */
@Composable
fun MenuContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    event: (HomeEvent) -> Unit = {},
    navController: NavHostController,
) {
    var pipeline by remember { mutableStateOf(0) }
    var syringe by remember { mutableStateOf(0) }
    var time by remember { mutableStateOf(0) }

    // Start a timer to display the runtime
    LaunchedEffect(key1 = uiState.loading) {
        while (true) {
            if (uiState.loading != 0) {
                time += 1
            } else {
                time = 0
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
        // Reset item
        item {
            FunctionCard(
                title = "复位",
                description = "依次复位Z轴、Y轴、注射泵",
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
                    onClick = { event(HomeEvent.Reset) }
                ) {
                    Text(
                        text = "开始复位",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        // Clean item
        item {
            FunctionCard(
                title = "管路清洗",
                description = "清洗管路残留胶体、促凝剂",
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
                        enabled = uiState.loading == 2,
                        onClick = { event(HomeEvent.Clean(0)) }
                    ) {
                        Text(
                            text = "取消",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = uiState.loading == 0,
                        onClick = { event(HomeEvent.Clean(1)) }
                    ) {
                        Text(
                            text = "开始",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

            }
        }
        // Syringe item
        item {
            FunctionCard(
                title = "促凝剂 ${if (syringe == 0) "填充" else "回吸"}",
                description = "填充/回吸促凝剂，点击按钮切换",
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
                            painter = painterResource(id = R.drawable.ic_syringe),
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
                                event(HomeEvent.Syringe(0))
                            } else {
                                syringe = if (syringe == 0) 1 else 0
                            }
                        }
                    ) {
                        if (uiState.loading == 3) {
                            Text(
                                text = "取消",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Serif,
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
                        onClick = { event(HomeEvent.Syringe(syringe + 1)) }
                    ) {
                        Text(
                            text = "开始",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

            }
        }
        // Pipeline item
        item {
            FunctionCard(
                title = "胶体 ${if (pipeline == 0) "填充" else "回吸"}",
                description = "填充/回吸胶体，点击按钮切换",
                image = {
                    if (uiState.loading == 4) {
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
                            if (uiState.loading == 4) {
                                event(HomeEvent.Pipeline(0))
                            } else {
                                pipeline = if (pipeline == 0) 1 else 0
                            }
                        }
                    ) {
                        if (uiState.loading == 4) {
                            Text(
                                text = "取消",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Serif,
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
                        onClick = { event(HomeEvent.Pipeline(pipeline + 1)) }
                    ) {
                        Text(
                            text = "开始",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

            }
        }
        // Start item
        item {
            FunctionCard(
                title = "程序运行",
                description = "选择并执行制胶程序",
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
                                    event(HomeEvent.NavTo(PageType.START))
                                }
                            }
                        }
                    ) {
                        Text(
                            text = "开始 ✔",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

            }
        }
    }
}

/**
 * Composable function for the start content of the Home screen.
 *
 * @param modifier The modifier to apply to the composable.
 * @param uiState The current UI state of the Home screen.
 * @param event The function to handle events on the Home screen.
 * @param toggleDrawer The function to toggle the drawer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    event: (HomeEvent) -> Unit = {},
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
        // Display the operation column
        MyTopAppBar(
            onBackPressed = { event(HomeEvent.NavTo(PageType.LIST)) },
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
        // Display the list of entities in a grid
        LazyVerticalGrid(
            modifier = modifier
                .fillMaxSize()
                .shadow(
                    elevation = 2.dp,
                    shape = MaterialTheme.shapes.medium,
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
                            // Toggle the selected state of the entity and navigate to the runtime page
                            event(HomeEvent.ToggleSelected(item.id))
                            event(HomeEvent.NavTo(PageType.RUNTIME))
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


/**
 * Composable function for the runtime content of the Home screen.
 *
 * @param modifier The modifier to apply to the composable.
 * @param uiState The current UI state of the Home screen.
 * @param event The function to handle events on the Home screen.
 * @param toggleDrawer The function to toggle the drawer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuntimeContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    event: (HomeEvent) -> Unit = {},
    toggleDrawer: (NavigationType) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var time by remember { mutableStateOf(0L) }
    val item = uiState.entities.find { it.id == uiState.selected }!!

    // Start a timer to display the runtime
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
        // Display the close button
        FloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd),
            onClick = {
                scope.launch {
                    if (uiState.job == null) {
                        event(HomeEvent.NavTo(PageType.LIST))
                        toggleDrawer(NavigationType.NAVIGATION_RAIL)
                    }
                }
            }
        ) {
            if (uiState.job == null) {
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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Display the runtime timer
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

                // Display the volume information
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
                // Display the start/stop button
                ElevatedCard(
                    onClick = {
                        if (uiState.job == null) {
                            if (uiState.loading == 0) {
                                event(HomeEvent.Start)
                            }
                        } else {
                            event(HomeEvent.Stop)
                        }
                    },
                ) {
                    if (uiState.job == null) {
                        if (uiState.loading == 0) {
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
                                    color = Color.Green,
                                )
                            }
                        }
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


/**
 * FunctionCard
 *
 * @param modifier Modifier
 * @param title String
 * @param description String
 * @param image [@androidx.compose.runtime.Composable] Function0<Unit>
 * @param button [@androidx.compose.runtime.Composable] Function0<Unit>
 * @return Unit
 */
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
                        fontFamily = FontFamily.Serif,
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

/**
 * Composable function for the preview of the list content of the Home screen.
 */
@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun MenuContentPreview() {
    MenuContent(
        uiState = HomeUiState(),
        navController = rememberNavController()
    )
}

/**
 * Composable function for the preview of the start content of the Home screen.
 */
@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun StartContentPreview() {
    // Create a list of program entities for the preview
    val entities = listOf(
        Program(),
        Program(),
    )

    // Display the start content with the preview entities
    StartContent(uiState = HomeUiState(entities = entities))
}

/**
 * Composable function for the preview of the runtime content of the Home screen.
 */
@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun RuntimeContentPreview() {
    // Create a list of program entities for the preview
    val entities = listOf(
        Program(id = 1),
        Program(),
    )

    // Display the runtime content with the preview entities and a selected entity
    RuntimeContent(uiState = HomeUiState(entities = entities, selected = 1L))
}