package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.ui.components.*
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.Constants
import com.zktony.android.utils.extra.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: SettingViewModel,
    snackbarHostState: SnackbarHostState,
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.SETTINGS -> navController.navigateUp()
                PageType.MOTOR_DETAIL -> viewModel.uiEvent(SettingUiEvent.NavTo(PageType.MOTOR_LIST))
                else -> viewModel.uiEvent(SettingUiEvent.NavTo(PageType.SETTINGS))
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
            SettingsAppBar(
                uiState = uiState,
                uiEvent = viewModel::uiEvent,
                navigation = navigation
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
    ) { paddingValues ->
        SettingsScreen(
            modifier = modifier.padding(paddingValues),
            uiState = uiState,
            uiEvent = viewModel::uiEvent,
            snackbarHostState = snackbarHostState
        )
    }
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    uiEvent: (SettingUiEvent) -> Unit = {},
    snackbarHostState: SnackbarHostState
) {
    AnimatedVisibility(visible = uiState.page == PageType.SETTINGS) {
        SettingsContent(modifier, uiState, uiEvent, snackbarHostState)
    }
    AnimatedVisibility(visible = uiState.page == PageType.AUTH) {
        Authentication(modifier, uiEvent)
    }
    AnimatedVisibility(visible = uiState.page == PageType.MOTOR_LIST) {
        MotorList(modifier, uiState, uiEvent)
    }
    AnimatedVisibility(visible = uiState.page == PageType.MOTOR_DETAIL) {
        MotorDetail(modifier, uiState, uiEvent)
    }
    AnimatedVisibility(visible = uiState.page == PageType.CONFIG) {
        ConfigList(modifier)
    }
}

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    uiEvent: (SettingUiEvent) -> Unit = {},
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var navigation by rememberDataSaverState(key = Constants.NAVIGATION, default = false)
    var helpInfo by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = MaterialTheme.shapes.small
                ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsCard(
                    icon = Icons.Outlined.Navigation,
                    text = stringResource(id = R.string.navigation)
                ) {
                    Switch(
                        modifier = Modifier.height(32.dp),
                        checked = navigation,
                        onCheckedChange = {
                            scope.launch {
                                navigation = it
                                uiEvent(SettingUiEvent.Navigation(it))
                            }
                        }
                    )
                }
            }

            item {
                SettingsCard(
                    icon = Icons.Outlined.Wifi,
                    text = stringResource(id = R.string.network),
                    onClick = { uiEvent(SettingUiEvent.Network) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowRight,
                        contentDescription = null
                    )
                }
            }

            item {
                SettingsCard(
                    icon = Icons.Outlined.Security,
                    text = stringResource(id = R.string.parameters),
                    onClick = { uiEvent(SettingUiEvent.NavTo(PageType.AUTH)) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowRight,
                        contentDescription = null
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = MaterialTheme.shapes.small
                ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsCard(
                    icon = Icons.Outlined.Info,
                    text = stringResource(id = R.string.version)
                ) {
                    Text(
                        text = BuildConfig.VERSION_NAME,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            item {
                SettingsCard(
                    icon = Icons.Outlined.HelpOutline,
                    text = if (helpInfo) stringResource(id = R.string.qrcode) else stringResource(id = R.string.help),
                    onClick = { helpInfo = !helpInfo },
                ) {
                    Icon(
                        imageVector = if (helpInfo) Icons.Default.Close else Icons.Default.ArrowRight,
                        contentDescription = null
                    )
                }
            }

            item {
                val image = if (uiState.application == null) {
                    Icons.Outlined.Sync
                } else {
                    if (uiState.application.version_code > BuildConfig.VERSION_CODE) {
                        Icons.Outlined.Grade
                    } else {
                        Icons.Outlined.Verified
                    }
                }

                val text = if (uiState.application == null) {
                    stringResource(id = R.string.update)
                } else {
                    if (uiState.progress == 0) {
                        if (uiState.application.version_code > BuildConfig.VERSION_CODE) {
                            stringResource(id = R.string.update_available)
                        } else {
                            stringResource(id = R.string.already_latest)
                        }
                    } else {
                        stringResource(id = R.string.downloading)
                    }
                }

                SettingsCard(
                    icon = image,
                    text = text,
                    onClick = {
                        scope.launch {
                            if (context.isNetworkAvailable()) {
                                uiEvent(SettingUiEvent.CheckUpdate)
                            } else {
                                snackbarHostState.showSnackbar(message = "网络不可用")
                            }
                        }
                    }
                ) {

                    if (uiState.application == null) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                    } else {
                        if (uiState.progress == 0) {
                            Icon(
                                imageVector = Icons.Default.ArrowCircleUp,
                                contentDescription = null
                            )
                        } else {
                            Text(
                                text = "${uiState.progress}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
            }

            if (helpInfo) {
                // Display the help info
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            modifier = Modifier
                                .size(200.dp)
                                .align(Alignment.Center),
                            painter = painterResource(id = R.mipmap.qrcode),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Authentication(
    modifier: Modifier = Modifier,
    uiEvent: (SettingUiEvent) -> Unit = {}
) {
    var show by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        AnimatedVisibility(visible = !show) {
            VerificationCodeField(digits = 6, inputCallback = {
                show = true
            }) { text, focused ->
                VerificationCodeItem(text, focused)
            }
        }
        AnimatedVisibility(visible = show) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ElevatedCard(onClick = { uiEvent(SettingUiEvent.NavTo(PageType.MOTOR_LIST)) }) {
                    Column(
                        modifier = Modifier.padding(horizontal = 64.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier.size(96.dp),
                            imageVector = Icons.Default.Cyclone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp),
                            text = stringResource(id = R.string.motor_config),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
                ElevatedCard(onClick = { uiEvent(SettingUiEvent.NavTo(PageType.CONFIG)) }) {
                    Column(
                        modifier = Modifier.padding(horizontal = 64.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier.size(96.dp),
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp),
                            text = stringResource(id = R.string.system_config),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCard(
    paddingStart: Dp = 0.dp,
    onClick: () -> Unit = { },
    icon: ImageVector,
    text: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.padding(start = paddingStart),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary
            )
            text?.let {
                Text(
                    text = text,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        letterSpacing = 0.15.sp
                    )
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            content.invoke()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorList(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    uiEvent: (SettingUiEvent) -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.small
            ),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        columns = GridCells.Fixed(3)
    ) {
        items(items = uiState.entities) {
            Card(
                onClick = {
                    scope.launch {
                        uiEvent(SettingUiEvent.ToggleSelected(it.id))
                        uiEvent(SettingUiEvent.NavTo(PageType.MOTOR_DETAIL))
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "M ${it.index}",
                        fontSize = 50.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Column(
                        modifier = Modifier.padding(start = 16.dp),
                    ) {
                        Text(
                            text = "A - ${it.acceleration}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = "D - ${it.deceleration}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = "S - ${it.speed}", style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun MotorDetail(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    uiEvent: (SettingUiEvent) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val softKeyboard = LocalSoftwareKeyboardController.current
    val selected = uiState.entities.find { it.id == uiState.selected }!!
    var ads by remember { mutableStateOf(selected.toAdsString()) }
    var index by remember { mutableStateOf(selected.index.toString()) }

    val keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done,
    )

    val keyboardActions = KeyboardActions(
        onDone = {
            softKeyboard?.hide()
        }
    )

    val colors = TextFieldDefaults.colors(
        unfocusedIndicatorColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
    )

    val textStyle = TextStyle(
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        fontFamily = FontFamily.Monospace,
    )

    LazyColumn(
        modifier = modifier
            .padding(16.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.medium
            )
            .windowInsetsPadding(WindowInsets.imeAnimationSource),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = index,
                onValueChange = {
                    scope.launch {
                        index = it
                        uiEvent(
                            SettingUiEvent.Update(
                                selected.copy(index = it.toIntOrNull() ?: 0)
                            )
                        )
                    }
                },
                leadingIcon = {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Icon(
                            imageVector = Icons.Default.Numbers,
                            contentDescription = null
                        )
                    }
                },
                suffix = {
                    Text(text = "电机编号", style = textStyle)
                },
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                shape = CircleShape,
                colors = colors,
                textStyle = textStyle,
            )
        }
        item {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = ads.first,
                onValueChange = {
                    scope.launch {
                        ads = Triple(it, ads.second, ads.third)
                        uiEvent(
                            SettingUiEvent.Update(
                                selected.copy(acceleration = it.toLongOrNull() ?: 0L)
                            )
                        )
                    }
                },
                leadingIcon = {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = stringResource(id = R.string.acceleration)
                        )
                    }
                },
                trailingIcon = {
                    ElevatedButton(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onClick = {
                            scope.launch {
                                writeRegister(selected.index, 152, selected.acceleration.toInt())
                                delay(500L)
                                writeRegister(selected.index, 220, 1)
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null)
                    }
                },
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                shape = CircleShape,
                colors = colors,
                textStyle = textStyle,
            )
        }
        item {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = ads.second,
                onValueChange = {
                    scope.launch {
                        ads = Triple(ads.first, it, ads.third)
                        uiEvent(
                            SettingUiEvent.Update(
                                selected.copy(
                                    deceleration = it.toLongOrNull() ?: 0L
                                )
                            )
                        )
                    }
                },
                leadingIcon = {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = stringResource(id = R.string.deceleration)
                        )
                    }
                },
                trailingIcon = {
                    ElevatedButton(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onClick = {
                            scope.launch {
                                writeRegister(selected.index, 153, selected.deceleration.toInt())
                                delay(500L)
                                writeRegister(selected.index, 220, 1)
                            }
                        }) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null)
                    }
                },
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                shape = CircleShape,
                colors = colors,
                textStyle = textStyle,
            )
        }
        item {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = ads.third,
                onValueChange = {
                    scope.launch {
                        ads = Triple(ads.first, ads.second, it)
                        uiEvent(
                            SettingUiEvent.Update(
                                selected.copy(
                                    speed = it.toLongOrNull() ?: 0L
                                )
                            )
                        )
                    }
                },
                leadingIcon = {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = stringResource(id = R.string.speed)
                        )
                    }
                },
                trailingIcon = {
                    ElevatedButton(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onClick = {
                            scope.launch {
                                writeRegister(selected.index, 154, selected.speed.toInt())
                                delay(500L)
                                writeRegister(selected.index, 220, 1)
                            }
                        }) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null)
                    }
                },
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                shape = CircleShape,
                colors = colors,
                textStyle = textStyle,
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConfigList(modifier: Modifier = Modifier) {

    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier
            .padding(16.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.medium
            )
            .windowInsetsPadding(WindowInsets.imeAnimationSource),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            var value by rememberDataSaverState(key = Constants.ZT_0000, default = 4)
            var string by remember { mutableStateOf(value.toString()) }

            CircleTextField(
                title = "模块数量",
                value = string,
                keyboardType = KeyboardType.Number,
                onValueChange = {
                    string = it
                    scope.launch { value = it.toIntOrNull() ?: 4 }
                }
            )
        }

        item {
            var value by rememberDataSaverState(key = Constants.ZT_0001, default = 4.0)
            var string by remember { mutableStateOf(value.toString()) }

            CircleTextField(
                title = "保温温度",
                value = string,
                onValueChange = {
                    string = it
                    scope.launch { value = it.toDoubleOrNull() ?: 0.0 }
                }
            )
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun SettingsPreview() {
    SettingsContent(uiState = SettingUiState(), snackbarHostState = SnackbarHostState())
}