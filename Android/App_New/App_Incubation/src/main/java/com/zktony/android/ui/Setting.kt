package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cyclone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Motor
import com.zktony.android.ui.components.CircleTextField
import com.zktony.android.ui.components.MotorItem
import com.zktony.android.ui.components.SettingsAppBar
import com.zktony.android.ui.components.VerificationCodeField
import com.zktony.android.ui.components.VerificationCodeItem
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.items
import com.zktony.android.ui.utils.toList
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils.writeRegister
import com.zktony.android.utils.extra.Application
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingRoute(viewModel: SettingViewModel) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current

    val application by viewModel.application.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val page by viewModel.page.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val entities = viewModel.entities.collectAsLazyPagingItems()
    val navigation: () -> Unit = {
        scope.launch {
            when (page) {
                PageType.SETTINGS -> navigationActions.navigateUp()
                PageType.MOTOR_DETAIL -> viewModel.dispatch(SettingIntent.NavTo(PageType.MOTOR_LIST))
                else -> viewModel.dispatch(SettingIntent.NavTo(PageType.SETTINGS))
            }
        }
    }

    BackHandler { navigation() }

    LaunchedEffect(key1 = message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dispatch(SettingIntent.Message(null))
        }
    }

    Column {
        SettingsAppBar(page, viewModel::dispatch) { navigation() }
        AnimatedContent(targetState = page) {
            when (page) {
                PageType.SETTINGS -> SettingContent(application, progress, viewModel::dispatch)
                PageType.AUTH -> Authentication(viewModel::dispatch)
                PageType.MOTOR_LIST -> MotorList(entities, viewModel::dispatch)
                PageType.MOTOR_DETAIL -> MotorDetail(
                    entities.toList(),
                    selected,
                    viewModel::dispatch
                )

                PageType.CONFIG -> ConfigList()
                else -> {}
            }
        }
    }
}

@Composable
fun SettingContent(
    application: Application?,
    progress: Int,
    dispatch: (SettingIntent) -> Unit
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
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = MaterialTheme.shapes.small
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
                                dispatch(SettingIntent.Navigation(it))
                            }
                        })
                }
            }

            item {
                SettingsCard(icon = Icons.Outlined.Wifi,
                    text = stringResource(id = R.string.network),
                    onClick = { dispatch(SettingIntent.Network) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowRight,
                        contentDescription = null
                    )
                }
            }

            item {
                SettingsCard(icon = Icons.Outlined.Security,
                    text = stringResource(id = R.string.parameters),
                    onClick = { dispatch(SettingIntent.NavTo(PageType.AUTH)) }) {
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
                val image = if (application == null) {
                    Icons.Outlined.Sync
                } else {
                    if (application.versionCode > BuildConfig.VERSION_CODE) {
                        Icons.Outlined.Grade
                    } else {
                        Icons.Outlined.Verified
                    }
                }

                val text = if (application == null) {
                    stringResource(id = R.string.update)
                } else {
                    if (progress == 0) {
                        if (application.versionCode > BuildConfig.VERSION_CODE) {
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
                            dispatch(SettingIntent.CheckUpdate)
                        } else {
                            snackbarHostState.showSnackbar(message = "网络不可用")
                        }
                    }
                }) {

                    if (application == null) {
                        Icon(
                            imageVector = Icons.Default.Check, contentDescription = null
                        )
                    } else {
                        if (progress == 0) {
                            Icon(
                                imageVector = Icons.Default.ArrowCircleUp, contentDescription = null
                            )
                        } else {
                            Text(
                                text = "$progress%",
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
        horizontalArrangement = Arrangement.spacedBy(8.dp))
    {
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
fun Authentication(dispatch: (SettingIntent) -> Unit) {

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
                                    dispatch(SettingIntent.NavTo(PageType.MOTOR_LIST))
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
                                    dispatch(SettingIntent.NavTo(PageType.CONFIG))
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
    entities: LazyPagingItems<Motor>,
    dispatch: (SettingIntent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    LazyVerticalGrid(
        modifier = Modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        columns = GridCells.Fixed(3)
    ) {
        items(items = entities) { item ->
            MotorItem(
                item = item,
                onClick = {
                    scope.launch {
                        dispatch(SettingIntent.ToggleSelected(item.id))
                        dispatch(SettingIntent.NavTo(PageType.MOTOR_DETAIL))
                    }
                },
                onDelete = {
                    scope.launch {
                        dispatch(SettingIntent.Delete(item.id))
                        snackbarHostState.showSnackbar(message = "删除成功")
                    }
                }
            )
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MotorDetail(
    entities: List<Motor>,
    selected: Long,
    dispatch: (SettingIntent) -> Unit
) {

    val scope = rememberCoroutineScope()
    val softKeyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = LocalSnackbarHostState.current

    val motor = entities.find { it.id == selected } ?: Motor(displayText = "None")
    var displayText by remember { mutableStateOf(motor.displayText) }
    var acceleration by remember { mutableStateOf(motor.acceleration.toString()) }
    var deceleration by remember { mutableStateOf(motor.deceleration.toString()) }
    var speed by remember { mutableStateOf(motor.speed.toString()) }
    var index by remember { mutableStateOf(motor.index.toString()) }

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
        focusedIndicatorColor = Color.Transparent,
    )

    val textStyle = TextStyle(
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        fontFamily = FontFamily.Monospace,
    )

    LazyColumn(
        modifier = Modifier
            .imePadding(),
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
                        dispatch(SettingIntent.Update(motor.copy(index = it.toIntOrNull() ?: 0)))
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
                value = displayText,
                onValueChange = {
                    scope.launch {
                        displayText = it
                        dispatch(SettingIntent.Update(motor.copy(displayText = it)))
                    }
                },
                leadingIcon = {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = null
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
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
                        dispatch(
                            SettingIntent.Update(
                                motor.copy(acceleration = it.toLongOrNull() ?: 0L)
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
                    ElevatedButton(modifier = Modifier.padding(horizontal = 16.dp), onClick = {
                        scope.launch {
                            writeRegister(motor.index, 152, motor.acceleration.toInt())
                            delay(500L)
                            writeRegister(motor.index, 220, 1)
                            snackbarHostState.showSnackbar(message = "已下载加速时间")
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
                value = deceleration,
                onValueChange = {
                    scope.launch {
                        deceleration = it
                        dispatch(
                            SettingIntent.Update(
                                motor.copy(
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
                    ElevatedButton(modifier = Modifier.padding(horizontal = 16.dp), onClick = {
                        scope.launch {
                            writeRegister(motor.index, 153, motor.deceleration.toInt())
                            delay(500L)
                            writeRegister(motor.index, 220, 1)
                            snackbarHostState.showSnackbar(message = "已下载减速时间")
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
                value = speed,
                onValueChange = {
                    scope.launch {
                        speed = it
                        dispatch(
                            SettingIntent.Update(
                                motor.copy(
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
                    ElevatedButton(modifier = Modifier.padding(horizontal = 16.dp), onClick = {
                        scope.launch {
                            writeRegister(motor.index, 154, motor.speed.toInt())
                            delay(500L)
                            writeRegister(motor.index, 220, 1)
                            snackbarHostState.showSnackbar(message = "已下载最大运行速度")
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


@Composable
fun ConfigList() {

    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .imePadding(),
        contentPadding = PaddingValues(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            var value by rememberDataSaverState(key = Constants.ZT_0000, default = 4)
            var string by remember { mutableStateOf(value.toString()) }

            CircleTextField(
                title = "模块数量",
                value = string,
                keyboardType = KeyboardType.Number
            ) {
                scope.launch {
                    string = it
                    value = it.toIntOrNull() ?: 0
                }
            }
        }

        item {
            var value by rememberDataSaverState(key = Constants.ZT_0001, default = 4.0)
            var string by remember { mutableStateOf(value.toString()) }

            CircleTextField(
                title = "保温温度",
                value = string
            ) {
                scope.launch {
                    string = it
                    value = it.toDoubleOrNull() ?: 0.0
                }
            }
        }

        item {
            var value by rememberDataSaverState(Constants.ZT_0002, 0.0)
            var string by remember { mutableStateOf(value.toString()) }

            CircleTextField(
                title = "补偿圈数",
                value = string,
                keyboardType = KeyboardType.Number
            ) {
                scope.launch {
                    string = it
                    value = it.toDoubleOrNull() ?: 0.0
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun SettingsPreview() {
    SettingContent(null, 0) {}
}