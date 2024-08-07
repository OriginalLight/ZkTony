package com.zktony.android.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.ui.components.CircularButtonsWithSelection
import com.zktony.android.ui.utils.NavigationType
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.utils.ext.format
import com.zktony.android.utils.tx.tx
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


var spStartNum = 0;

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
    val spCount by viewModel.spCount.collectAsStateWithLifecycle()

    // Handle the back ElevatedButton press
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
        spCount = spCount,
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
    spCount: Int = 0,
    toggleDrawer: (NavigationType) -> Unit = {},
    navController: NavHostController,
) {
    var time by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current

    val context = LocalContext.current;

    /**
     * 加液体积
     */
    val tiji = rememberDataSaverState(key = "tiji", default = 0f)
    var tiji_ex by remember { mutableStateOf(tiji.value.format(1)) }


    /**
     * 加液体积2
     */
    val tiji2 = rememberDataSaverState(key = "tiji2", default = 0f)
    var tiji_ex2 by remember { mutableStateOf(tiji2.value.format(1)) }


    /**
     * 紫外状态-默认关闭
     * false关
     * true开
     */
    val uvState = rememberDataSaverState(key = "uvState", default = false)
    var uvState_ex by remember { mutableStateOf(false) }


    val valveOne = rememberDataSaverState(key = "valveOne", default = 0)
    var valveOne_ex by remember { mutableStateOf(0) }


    /**
     * 判断是否复位
     * true=复位完成
     * false=没复位
     */
    val isResetBool = rememberDataSaverState(key = "isResetBool", default = false)
    var isResetBool_ex by remember { mutableStateOf(false) }


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

    LaunchedEffect(key1 = spCount) {
        if (uiState.loading != 0) {
            valveOne.value = spCount
        }
    }

    Row(
        modifier = modifier.fillMaxSize(),
    ) {


        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        ) {
            Row(
                modifier = Modifier
                    .height(30.dp)
                    .width(800.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "培养皿(" + count + ")",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
            Row(
                modifier = Modifier
                    .height(570.dp)
                    .width(800.dp)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularButtonsWithSelection(
                        buttonEnabled = uiState.uiFlags != UiFlags.VALVE,
                        selectedButtonIndex = valveOne.value
                    ) {
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .width(200.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .width(140.dp)
                    .padding(start = 20.dp, top = 50.dp),
                value = tiji_ex,
                onValueChange = {
                    scope.launch {
                        tiji_ex = it
                        tiji.value = it.toFloatOrNull() ?: 0f
                    }
                },
                label = { Text(text = "体积1/μL") },
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

            OutlinedTextField(
                modifier = Modifier
                    .width(140.dp)
                    .padding(start = 20.dp, top = 10.dp),
                value = tiji_ex2,
                onValueChange = {
                    scope.launch {
                        tiji_ex2 = it
                        tiji2.value = it.toFloatOrNull() ?: 0f
                    }
                },
                label = { Text(text = "体积2/μL") },
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

            ElevatedButton(
                modifier = Modifier
                    .width(140.dp)
                    .padding(start = 20.dp, top = 10.dp),
                enabled = uiState.loading == 0 || uiState.loading == 7,
                onClick = {
                    if (isResetBool.value) {
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
                    } else {
                        Toast.makeText(
                            context,
                            "复位后再运动!",
                            Toast.LENGTH_SHORT
                        ).show()
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

            ElevatedButton(
                modifier = Modifier
                    .width(140.dp)
                    .padding(start = 20.dp, top = 10.dp),

                enabled = uiState.loading == 0,
                onClick = {
//                    event(HomeEvent.spStart(valveOne.value))
                    if (isResetBool.value) {
                        if (spStartNum < 8) {
                            if (valveOne.value == 7) {
                                valveOne.value = 0
                            } else {
                                valveOne.value += 1
                                valveOne_ex += 1
                            }
                            spStartNum += 1
                            event(HomeEvent.spStart(valveOne.value))
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "复位后再运动!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            ) {
                Text(
                    text = "上盘",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                )
            }

            ElevatedButton(
                modifier = Modifier
                    .width(140.dp)
                    .padding(start = 20.dp, top = 10.dp),
                enabled = uiState.loading == 2,
                onClick = {
                    event(HomeEvent.xpStart)
                }
            ) {
                Text(
                    text = "下盘",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                )
            }

            ElevatedButton(
                modifier = Modifier
                    .width(140.dp)
                    .padding(start = 20.dp, top = 10.dp),

                enabled = uiState.loading == 0 || uiState.loading == 2,
                onClick = {
                    event(HomeEvent.Reset)
                    valveOne.value = 0
                    valveOne_ex = 0
                    isResetBool.value = true
                    isResetBool_ex = true
                    spStartNum = 0
                }
            ) {
                Text(
                    text = "复位",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                )
            }

            ElevatedButton(
                modifier = Modifier
                    .width(140.dp)
                    .padding(start = 20.dp, top = 10.dp),

                enabled = uiState.loading == 0 || uiState.loading == 7,
                onClick = {
                    if (!uvState.value) {
                        uvState.value = true
                        uvState_ex = true
                        scope.launch {
                            tx {
                                valve(0 to 1)
                            }
                        }
                    } else {
                        uvState.value = false
                        uvState_ex = false
                        scope.launch {
                            tx {
                                valve(0 to 0)
                            }
                        }
                    }
                }
            ) {
                if (!uvState.value) {
                    Text(
                        text = "紫外(关)",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                    )
                } else {
                    Text(
                        text = "紫外(开)",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }



            ElevatedButton(
                modifier = Modifier
                    .width(140.dp)
                    .padding(start = 20.dp, top = 10.dp),

                enabled = uiState.loading == 0 || uiState.loading == 4,
                onClick = {
                    if (uiState.loading == 0) {
                        event(HomeEvent.PumpingOrRecrement(4))
                    } else {
                        event(HomeEvent.PumpingOrRecrement(3))
                    }
                }
            ) {

                if (uiState.loading == 4) {
                    Text(
                        text = "停止",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                    )
                } else {
                    Text(
                        text = "排液",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                    )
                }


            }

            ElevatedButton(
                modifier = Modifier
                    .width(140.dp)
                    .padding(start = 20.dp, top = 10.dp),

                enabled = uiState.loading == 0 || uiState.loading == 5,
                onClick = {
                    if (uiState.loading == 0) {
                        event(HomeEvent.PumpingOrRecrement(5))
                    } else {
                        event(HomeEvent.PumpingOrRecrement(6))
                    }
                }
            ) {
                if (uiState.loading == 5) {
                    Text(
                        text = "停止",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                    )
                } else {
                    Text(
                        text = "回吸",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
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
    ElevatedButton: @Composable () -> Unit,
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
            ElevatedButton()

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