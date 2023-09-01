package com.zktony.android.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.zktony.android.R
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.ui.utils.NavigationType
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.ext.format
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

    // Observe the UI state from the view model
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val count by viewModel.count.collectAsStateWithLifecycle()
    // Handle the back button press
    BackHandler {
        when (uiState.page) {
            PageType.START -> viewModel.event(HomeEvent.NavTo(PageType.LIST))
            else -> {}
        }
    }

    MenuContent(
        modifier = modifier,
        uiState = uiState,
        event = viewModel::event,
        count = count,
        navController = navController,
        toggleDrawer = toggleDrawer,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MenuContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    event: (HomeEvent) -> Unit = {},
    count: Int = 0,
    toggleDrawer: (NavigationType) -> Unit = {},
    navController: NavHostController,
) {
    var pipeline by remember { mutableStateOf(0) }
    var syringe by remember { mutableStateOf(0) }
    var time by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current

    val context = LocalContext.current;

    /**
     * 加液体积
     */
    val tiji = rememberDataSaverState(key = "tiji", default = 0f)
    var tiji_ex by remember { mutableStateOf(tiji.value.format(1)) }

    var spRunIndex = 1

    // Start a timer to display the runtime
    LaunchedEffect(key1 = uiState.loading) {
        if (uiState.loading == 0) {
            toggleDrawer(NavigationType.NAVIGATION_RAIL)
        } else {
            toggleDrawer(NavigationType.NONE)
        }
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
                description = "依次复位举升，下盘，上盘",
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
                    onClick = {
                        event(HomeEvent.Reset)
                        spRunIndex = 1
                    }
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
                title = "紫外",
                description = "",
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
                            painter = painterResource(id = R.drawable.ic_uv),
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
                title = "${if (syringe == 0) "排液" else "回吸"}",
                description = "排液/回吸，点击按钮切换",
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
        // Start item
        item {
            FunctionCard(
                title = "程序运行(" + count +
                        ")",
                description =
                "当前停止不是即停,需要等当前举升1上方所有培养血清空后停止，如需即停，请关闭电源",
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
                            event(HomeEvent.spStart(spRunIndex))
                            spRunIndex += 1

                        }
                    ) {
                        Text(
                            text = "上盘",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = uiState.loading == 0 || uiState.loading == 7,
                        onClick = {
                            if (tiji.value.toFloat() != 0f) {
                                if (uiState.loading == 0) {
                                    event(HomeEvent.Start(7))
                                } else {
                                    event(HomeEvent.Start(8))
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "加液量不能为0!",
                                    Toast.LENGTH_SHORT
                                ).show();
                            }

                        }
                    ) {
                        if (uiState.loading == 7) {
                            Text(
                                text = "停止",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                            )
                        } else {
                            Text(
                                text = "运行",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                    }

                    OutlinedTextField(
                        modifier = Modifier
                            .width(70.dp),
                        value = tiji_ex,
                        onValueChange = {
                            scope.launch {
                                tiji_ex = it
                                tiji.value = it.toFloatOrNull() ?: 0f
                            }
                        },
                        label = { Text(text = "体积") },
                        shape = MaterialTheme.shapes.medium,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboard?.hide()
                            }
                        ),
                    )

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