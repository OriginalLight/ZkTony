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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Coordinate
import com.zktony.android.ui.components.*
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.Constants
import com.zktony.android.utils.extra.isNetworkAvailable
import com.zktony.android.utils.extra.serial
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

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
            SettingsAppBar {
                ElevatedButton(onClick = navigation) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = null
                    )
                }
            }
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
                        text = it.text,
                        fontSize = 50.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Column(
                        modifier = Modifier.padding(start = 16.dp),
                    ) {
                        Text(
                            text = "S - ${it.speed}", style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = "A - ${it.acc}", style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = "D - ${it.dec}", style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MotorDetail(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    uiEvent: (SettingUiEvent) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val entity = uiState.entities.find { it.id == uiState.selected }!!
    var speed by remember { mutableLongStateOf(entity.speed) }
    var acc by remember { mutableLongStateOf(entity.acc) }
    var dec by remember { mutableLongStateOf(entity.dec) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.small
            )
            .padding(32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(36.dp),
                imageVector = Icons.Default.Speed,
                contentDescription = stringResource(id = R.string.speed),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = "S - $speed",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = speed.toFloat(),
                onValueChange = { speed = it.roundToLong() },
                valueRange = 0f..800f,
                steps = 79,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(36.dp),
                imageVector = Icons.Default.TrendingUp,
                contentDescription = stringResource(id = R.string.acceleration),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = "A - $acc",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = acc.toFloat(),
                onValueChange = { acc = it.roundToLong() },
                valueRange = 0f..800f,
                steps = 79,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(36.dp),
                imageVector = Icons.Default.TrendingDown,
                contentDescription = stringResource(id = R.string.deceleration),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = "D - $dec",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = dec.toFloat(),
                onValueChange = { dec = it.roundToLong() },
                valueRange = 0f..800f,
                steps = 79,
            )
        }

        AnimatedVisibility(visible = entity.speed != speed || entity.acc != acc || entity.dec != dec) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                FloatingActionButton(
                    modifier = Modifier.width(192.dp),
                    onClick = {
                        scope.launch {
                            uiEvent(
                                SettingUiEvent.Update(
                                    entity.copy(
                                        speed = speed,
                                        acc = acc,
                                        dec = dec
                                    )
                                )
                            )
                            uiEvent(SettingUiEvent.NavTo(PageType.MOTOR_LIST))
                        }
                    },
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.Done,
                        contentDescription = null
                    )
                }
            }
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
            var abscissa by rememberDataSaverState(key = Constants.ZT_0001, default = 0.0)
            var ordinate by rememberDataSaverState(key = Constants.ZT_0002, default = 0.0)
            var tankAbscissa by rememberDataSaverState(
                key = Constants.ZT_0003,
                default = 0.0
            )
            var tankOrdinate by rememberDataSaverState(
                key = Constants.ZT_0004,
                default = 0.0
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CoordinateInput(
                    modifier = Modifier.weight(1f),
                    title = "行程",
                    coordinate = Coordinate(abscissa = abscissa, ordinate = ordinate),
                    onCoordinateChange = {
                        scope.launch {
                            abscissa = it.abscissa
                            ordinate = it.ordinate
                        }
                    }
                ) {
                    scope.launch {
                        serial {
                            start(index = 0, pdv = abscissa)
                            start(index = 1, pdv = ordinate)
                        }
                    }
                }
                CoordinateInput(
                    modifier = Modifier.weight(1f),
                    title = "废液槽",
                    coordinate = Coordinate(abscissa = tankAbscissa, ordinate = tankOrdinate),
                    onCoordinateChange = {
                        scope.launch {
                            tankAbscissa = it.abscissa
                            tankOrdinate = it.ordinate
                        }
                    }
                ) {
                    scope.launch {
                        serial {
                            start(index = 0, pdv = tankAbscissa)
                            start(index = 1, pdv = tankOrdinate)
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun SettingsPreview() {
    SettingsContent(uiState = SettingUiState(), snackbarHostState = SnackbarHostState())
}