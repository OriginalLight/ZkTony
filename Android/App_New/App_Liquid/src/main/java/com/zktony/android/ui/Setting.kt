package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Motor
import com.zktony.android.data.entities.internal.Point
import com.zktony.android.ui.components.*
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils.start
import com.zktony.android.utils.extra.*
import kotlinx.coroutines.launch

@Composable
fun SettingRoute(viewModel: SettingViewModel) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.SETTINGS -> navigationActions.navigateUp()
                PageType.MOTOR_DETAIL -> viewModel.uiEvent(SettingUiEvent.NavTo(PageType.MOTOR_LIST))
                else -> viewModel.uiEvent(SettingUiEvent.NavTo(PageType.SETTINGS))
            }
        }
    }

    BackHandler { navigation() }

    LaunchedEffect(key1 = message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.uiEvent(SettingUiEvent.Message(null))
        }
    }

    SettingWrapper(
        uiState = uiState, uiEvent = viewModel::uiEvent, navigation = navigation
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingWrapper(
    uiState: SettingUiState, uiEvent: (SettingUiEvent) -> Unit, navigation: () -> Unit
) {
    Column {
        SettingsAppBar(uiState, uiEvent) { navigation() }
        AnimatedContent(targetState = uiState.page) {
            when (uiState.page) {
                PageType.SETTINGS -> SettingContent(uiState, uiEvent)
                PageType.AUTH -> Authentication(uiEvent)
                PageType.MOTOR_LIST -> MotorList(uiState, uiEvent)
                PageType.MOTOR_DETAIL -> MotorDetail(uiState, uiEvent)
                PageType.CONFIG -> ConfigList()
                else -> {}
            }
        }
    }
}

@Composable
fun SettingContent(
    uiState: SettingUiState,
    uiEvent: (SettingUiEvent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current
    var navigation by rememberDataSaverState(key = Constants.NAVIGATION, default = false)
    var helpInfo by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = 1.dp, color = Color.LightGray, shape = MaterialTheme.shapes.small
                )
                .animateContentSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsCard(
                    icon = Icons.Outlined.Navigation,
                    text = stringResource(id = R.string.navigation)
                ) {
                    Switch(modifier = Modifier.height(32.dp),
                        checked = navigation,
                        onCheckedChange = {
                            scope.launch {
                                navigation = it
                                uiEvent(SettingUiEvent.Navigation(it))
                            }
                        })
                }
            }

            item {
                SettingsCard(icon = Icons.Outlined.Wifi,
                    text = stringResource(id = R.string.network),
                    onClick = { uiEvent(SettingUiEvent.Network) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowRight, contentDescription = null
                    )
                }
            }

            item {
                SettingsCard(icon = Icons.Outlined.Security,
                    text = stringResource(id = R.string.parameters),
                    onClick = { uiEvent(SettingUiEvent.NavTo(PageType.AUTH)) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowRight, contentDescription = null
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = 1.dp, color = Color.LightGray, shape = MaterialTheme.shapes.small
                )
                .animateContentSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsCard(
                    icon = Icons.Outlined.Info, text = stringResource(id = R.string.version)
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
                SettingsCard(icon = Icons.Outlined.HelpOutline,
                    text = if (helpInfo) stringResource(id = R.string.qrcode) else stringResource(id = R.string.help),
                    onClick = { helpInfo = !helpInfo }) {
                    if (helpInfo) {
                        Text(
                            text = "025-68790636",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ArrowRight, contentDescription = null
                        )
                    }
                }
            }

            item {
                val image = if (uiState.application == null) {
                    Icons.Outlined.Sync
                } else {
                    if (uiState.application.versionCode > BuildConfig.VERSION_CODE) {
                        Icons.Outlined.Grade
                    } else {
                        Icons.Outlined.Verified
                    }
                }

                val text = if (uiState.application == null) {
                    stringResource(id = R.string.update)
                } else {
                    if (uiState.progress == 0) {
                        if (uiState.application.versionCode > BuildConfig.VERSION_CODE) {
                            stringResource(id = R.string.update_available)
                        } else {
                            stringResource(id = R.string.already_latest)
                        }
                    } else {
                        stringResource(id = R.string.downloading)
                    }
                }

                SettingsCard(icon = image, text = text, onClick = {
                    scope.launch {
                        if (ApplicationUtils.isNetworkAvailable()) {
                            uiEvent(SettingUiEvent.CheckUpdate)
                        } else {
                            snackbarHostState.showSnackbar(message = "网络不可用")
                        }
                    }
                }) {

                    if (uiState.application == null) {
                        Icon(
                            imageVector = Icons.Default.Check, contentDescription = null
                        )
                    } else {
                        if (uiState.progress == 0) {
                            Icon(
                                imageVector = Icons.Default.ArrowCircleUp, contentDescription = null
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

@Composable
fun SettingsCard(
    onClick: () -> Unit = { },
    icon: ImageVector,
    text: String? = null,
    content: @Composable () -> Unit
) {
    Row(modifier = Modifier
        .background(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        )
        .clip(MaterialTheme.shapes.medium)
        .clickable { onClick() }
        .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            modifier = Modifier.size(32.dp),
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.primary
        )
        text?.let {
            Text(
                text = text, style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        content.invoke()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Authentication(uiEvent: (SettingUiEvent) -> Unit) {

    val scope = rememberCoroutineScope()
    var show by remember { mutableStateOf(false) }

    AnimatedContent(targetState = show) {
        if (it) {
            LazyVerticalGrid(
                contentPadding = PaddingValues(16.dp),
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ListItem(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                scope.launch {
                                    uiEvent(SettingUiEvent.NavTo(PageType.MOTOR_LIST))
                                }
                            },
                        headlineContent = {
                            Text(
                                text = stringResource(id = R.string.motor_config),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.Cyclone,
                                contentDescription = null
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
                item {
                    ListItem(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                scope.launch {
                                    uiEvent(SettingUiEvent.NavTo(PageType.CONFIG))
                                }
                            },
                        headlineContent = {
                            Text(
                                text = stringResource(id = R.string.system_config),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                contentAlignment = Alignment.Center
            ) {
                VerificationCodeField(digits = 6, inputCallback = {
                    show = true
                }) { text, focused ->
                    VerificationCodeItem(text, focused)
                }
            }
        }
    }
}

@Composable
fun MotorList(
    uiState: SettingUiState, uiEvent: (SettingUiEvent) -> Unit
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = Modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        columns = GridCells.Fixed(3)
    ) {
        items(items = uiState.entities) { item ->
            MotorItem(
                item = item,
                selected = uiState.selected == item.id
            ) { double ->
                scope.launch {
                    if (double) {
                        uiEvent(SettingUiEvent.ToggleSelected(item.id))
                        uiEvent(SettingUiEvent.NavTo(PageType.MOTOR_DETAIL))
                    } else {
                        if (uiState.selected != item.id) {
                            uiEvent(SettingUiEvent.ToggleSelected(item.id))
                        } else {
                            uiEvent(SettingUiEvent.ToggleSelected(0L))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MotorDetail(
    uiState: SettingUiState, uiEvent: (SettingUiEvent) -> Unit
) {

    val scope = rememberCoroutineScope()
    val softKeyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val selected = uiState.entities.find { it.id == uiState.selected } ?: Motor()
    var acceleration by remember { mutableStateOf(selected.acceleration.toString()) }
    var deceleration by remember { mutableStateOf(selected.deceleration.toString()) }
    var speed by remember { mutableStateOf(selected.speed.toString()) }
    var index by remember { mutableStateOf(selected.index.toString()) }

    val keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done,
    )

    val keyboardActions = KeyboardActions(onDone = {
        softKeyboard?.hide()
        focusManager.clearFocus()
    })

    val colors = TextFieldDefaults.colors(
        unfocusedIndicatorColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent
    )

    val textStyle = TextStyle(
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        fontFamily = FontFamily.Monospace
    )

    LazyColumn(
        modifier = Modifier.imePadding(),
        contentPadding = PaddingValues(32.dp),
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
                            imageVector = Icons.Default.Numbers, contentDescription = null
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
                value = acceleration,
                onValueChange = {
                    scope.launch {
                        acceleration = it
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
                suffix = {
                    Text(text = stringResource(id = R.string.acceleration), style = textStyle)
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
                value = deceleration,
                onValueChange = {
                    scope.launch {
                        deceleration = it
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
                suffix = {
                    Text(text = stringResource(id = R.string.deceleration), style = textStyle)
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
                value = speed,
                onValueChange = {
                    scope.launch {
                        speed = it
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
                suffix = {
                    Text(text = stringResource(id = R.string.speed), style = textStyle)
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


@Composable
fun ConfigList(modifier: Modifier = Modifier) {

    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier.imePadding(),
        contentPadding = PaddingValues(32.dp),
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
                    point = Point(x = abscissa, y = ordinate),
                    onCoordinateChange = {
                        scope.launch {
                            abscissa = it.x
                            ordinate = it.y
                        }
                    }
                ) {
                    scope.launch {
                        start {
                            with(index = 0, pdv = abscissa)
                            with(index = 1, pdv = ordinate)
                        }
                    }
                }
                CoordinateInput(
                    modifier = Modifier.weight(1f),
                    title = "废液槽",
                    point = Point(x = tankAbscissa, y = tankOrdinate),
                    onCoordinateChange = {
                        scope.launch {
                            tankAbscissa = it.x
                            tankOrdinate = it.y
                        }
                    }
                ) {
                    scope.launch {
                        start {
                            with(index = 0, pdv = tankAbscissa)
                            with(index = 1, pdv = tankOrdinate)
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
    SettingContent(uiState = SettingUiState()) {}
}
