package com.zktony.android.ui.screen

import android.annotation.SuppressLint
import android.graphics.Color
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncLock
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.ui.components.ZkTonyScaffold
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.PageEnum
import com.zktony.core.ext.Ext
import com.zktony.core.ext.createQRCodeBitmap
import com.zktony.core.utils.QrCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Setting screen
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param viewModel SettingViewModel
 */
@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: SettingViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }


    BackHandler {
        if (uiState.page == PageEnum.MAIN) {
            navController.navigateUp()
        } else {
            viewModel.navigationTo(PageEnum.MAIN)
        }
    }

    ZkTonyScaffold(
        modifier = modifier,
        topBar = {
            AnimatedVisibility(visible = uiState.page == PageEnum.AUTHENTICATION) {
                ZkTonyTopAppBar(title = stringResource(id = R.string.authentication), navigation = {
                    if (uiState.page == PageEnum.MAIN) {
                        navController.navigateUp()
                    } else {
                        viewModel.navigationTo(PageEnum.MAIN)
                    }
                })
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        AnimatedVisibility(visible = uiState.page == PageEnum.MAIN) {
            SettingMainPage(
                modifier = modifier,
                uiState = uiState,
                checkUpdate = viewModel::checkUpdate,
                navigationTo = viewModel::navigationTo,
                openWifi = viewModel::openWifi,
                setLanguage = viewModel::setLanguage,
                setNavigation = viewModel::setNavigation,
            )
        }
        AnimatedVisibility(visible = uiState.page == PageEnum.AUTHENTICATION) {
            AuthenticationPage(
                modifier = modifier,
                navController = navController,
                navigationTo = viewModel::navigationTo,
            )
        }
    }
}

@Composable
fun SettingMainPage(
    modifier: Modifier = Modifier,
    checkUpdate: () -> Unit = {},
    navigationTo: (PageEnum) -> Unit = {},
    openWifi: () -> Unit = {},
    setLanguage: (String) -> Unit = {},
    setNavigation: (Boolean) -> Unit = {},
    uiState: SettingUiState,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f),
        ) {
            SettingsContent(
                modifier = Modifier.weight(1f),
                setLanguage = setLanguage,
                setNavigation = setNavigation,
                uiState = uiState,
            )
            InfoContent(
                modifier = Modifier.weight(1f),
            )
        }
        OperationContent(
            modifier = Modifier.wrapContentHeight(),
            checkUpdate = checkUpdate,
            navigationTo = navigationTo,
            openWifi = openWifi,
            uiState = uiState,
        )
    }
}

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    setLanguage: (String) -> Unit = {},
    setNavigation: (Boolean) -> Unit = {},
    uiState: SettingUiState,
) {
    val lazyColumnState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val languageList = listOf(Pair("English", "en"), Pair("简体中文", "zh"))

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(end = 4.dp, bottom = 4.dp)
            .animateContentSize()
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium,
            ),
        state = lazyColumnState,
    ) {
        item {
            SettingsCard(
                icon = Icons.Default.Language,
                text = stringResource(id = R.string.language),
                onClick = { expanded = !expanded }) {
                Text(
                    text = when (uiState.settings.language) {
                        "en" -> "English"
                        "zh" -> "简体中文"
                        else -> "简体中文"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        languageList.forEach { (name, code) ->
            item {
                AnimatedVisibility(visible = expanded) {
                    SettingsCard(
                        icon = Icons.Default.Language,
                        paddingStart = 32.dp,
                        onClick = {
                            scope.launch {
                                setLanguage(code)
                                expanded = false
                            }
                        }) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
        item {
            SettingsCard(
                icon = Icons.Default.Navigation,
                text = stringResource(id = R.string.navigation),
            ) {
                Switch(
                    checked = uiState.settings.navigation,
                    onCheckedChange = {
                        scope.launch {
                            setNavigation(it)
                        }
                    },
                )
            }
        }
    }
}

@SuppressLint("HardwareIds")
@Composable
fun InfoContent(
    modifier: Modifier = Modifier,
) {
    val lazyColumnState = rememberLazyListState()
    var deviceInfo by remember { mutableStateOf(false) }
    var helpInfo by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 4.dp, bottom = 4.dp)
            .animateContentSize()
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        state = lazyColumnState,
    ) {
        item {
            AnimatedVisibility(visible = !deviceInfo && !helpInfo) {
                SettingsCard(
                    icon = Icons.Default.Info,
                    text = stringResource(id = R.string.version),
                ) {
                    Text(
                        text = BuildConfig.VERSION_NAME,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                    )
                }
            }
        }
        item {
            AnimatedVisibility(visible = !deviceInfo && !helpInfo) {
                SettingsCard(
                    icon = Icons.Default.Devices,
                    text = stringResource(id = R.string.device_info),
                    onClick = { deviceInfo = !deviceInfo },
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                    )
                }
            }

        }
        item {
            AnimatedVisibility(visible = !deviceInfo && !helpInfo) {
                SettingsCard(
                    icon = Icons.Default.Help,
                    text = stringResource(id = R.string.help),
                    onClick = { helpInfo = !helpInfo },
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                    )
                }
            }
        }
        item {
            AnimatedVisibility(visible = deviceInfo) {
                SettingsCard(
                    icon = Icons.Default.Devices,
                    text = stringResource(id = R.string.device_info),
                    onClick = { deviceInfo = !deviceInfo },
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                    )
                }
            }
        }
        item {
            AnimatedVisibility(visible = deviceInfo) {
                Card(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        val code = Gson().toJson(
                            QrCode(
                                id = Settings.Secure.getString(
                                    Ext.ctx.contentResolver, Settings.Secure.ANDROID_ID
                                ),
                                `package` = BuildConfig.APPLICATION_ID,
                                version_name = BuildConfig.VERSION_NAME,
                                version_code = BuildConfig.VERSION_CODE,
                                build_type = BuildConfig.BUILD_TYPE,
                            )
                        )
                        val bitmap = createQRCodeBitmap(
                            content = code,
                            width = 400,
                            height = 400,
                            character_set = "UTF-8",
                            error_correction_level = "H",
                            margin = "1",
                            color_black = Color.BLACK,
                            color_white = Color.WHITE
                        )

                        if (bitmap != null) {
                            Image(
                                modifier = Modifier.size(200.dp),
                                painter = BitmapPainter(bitmap.asImageBitmap()),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
        item {
            AnimatedVisibility(visible = helpInfo) {
                SettingsCard(
                    icon = Icons.Default.Help,
                    text = stringResource(id = R.string.qrcode),
                    onClick = { helpInfo = !helpInfo },
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                    )
                }
            }
        }
        item {
            AnimatedVisibility(visible = helpInfo) {
                Card(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Image(
                            modifier = Modifier.size(200.dp),
                            painter = painterResource(id = R.drawable.qrcode),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OperationContent(
    modifier: Modifier = Modifier,
    checkUpdate: () -> Unit = {},
    navigationTo: (PageEnum) -> Unit = {},
    openWifi: () -> Unit = {},
    uiState: SettingUiState,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium,
            )
            .animateContentSize(),
    ) {
        ElevatedCard(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp, vertical = 8.dp)
                .clickable { navigationTo(PageEnum.AUTHENTICATION) },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    modifier = Modifier.size(96.dp),
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = stringResource(id = R.string.parameters),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        ElevatedCard(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp, vertical = 8.dp)
                .clickable { openWifi() },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    modifier = Modifier.size(96.dp),
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = stringResource(id = R.string.wifi),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        ElevatedCard(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp, vertical = 8.dp)
                .clickable { checkUpdate() },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val icon = if (uiState.application == null) {
                    Icons.Default.Sync
                } else {
                    if (uiState.application.versionCode > BuildConfig.VERSION_CODE) {
                        Icons.Default.NewReleases
                    } else {
                        Icons.Default.SyncLock
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
                        "${uiState.progress} %"
                    }
                }
                AnimatedVisibility(visible = uiState.progress > 0) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(96.dp)
                            .padding(8.dp),
                        progress = uiState.progress / 100f,
                        strokeWidth = 16.dp,
                    )
                }
                AnimatedVisibility(visible = uiState.progress == 0) {
                    Icon(
                        modifier = Modifier.size(96.dp),
                        imageVector = icon,
                        contentDescription = text,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
fun VerificationCodeItem(text: String, focused: Boolean) {
    val borderColor = if (focused) {
        MaterialTheme.colorScheme.onBackground
    } else {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
    }

    Box(
        modifier = Modifier
            .border(4.dp, borderColor, RoundedCornerShape(8.dp))
            .size(64.dp, 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text, fontSize = 28.sp, textAlign = TextAlign.Center, maxLines = 1
        )
    }

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VerificationCodeField(
    digits: Int,
    horizontalMargin: Dp = 16.dp,
    inputCallback: (content: String) -> Unit = {},
    itemScope: @Composable (text: String, focused: Boolean) -> Unit,
) {
    var content by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(100) //延迟操作(关键点)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Box {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //绘制框
            repeat(digits) {
                if (it != 0) {
                    //添加间距
                    Spacer(modifier = Modifier.width(horizontalMargin))
                }
                //获取当前框的文本
                val text = content.getOrNull(it)?.toString() ?: ""
                //是否正在输入的框
                val focused = it == content.length
                //绘制文本
                itemScope(text, focused)
            }

        }
        BasicTextField(
            modifier = Modifier
                .focusRequester(focusRequester)
                .drawWithContent { }//清楚绘制内容
                .matchParentSize(),
            value = content,
            onValueChange = {
                content = it
                if (it.length == digits) {
                    if (it == "123456") {
                        inputCallback(it)
                        keyboardController?.hide()
                    } else {
                        content = ""
                        focusRequester.requestFocus()
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
            }),
        )
    }

}

@Composable
fun AuthenticationPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    navigationTo: (PageEnum) -> Unit = {},
) {
    var show by remember { mutableStateOf(false) }

    BackHandler {
        navigationTo(PageEnum.MAIN)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(128.dp))
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
                ElevatedCard(
                    modifier = Modifier.clickable {
                        navigationTo(PageEnum.MAIN)
                        navController.navigate(Route.MOTOR)
                    },
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 64.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            modifier = Modifier.size(96.dp),
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(id = R.string.motor_config),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp),
                            text = stringResource(id = R.string.motor_config),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                ElevatedCard(
                    modifier = Modifier.clickable {
                        navigationTo(PageEnum.MAIN)
                        navController.navigate(Route.CONFIG)
                    },
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 64.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            modifier = Modifier.size(96.dp),
                            imageVector = Icons.Default.Description,
                            contentDescription = stringResource(id = R.string.system_config),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp),
                            text = stringResource(id = R.string.system_config),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCard(
    paddingStart: Dp = 8.dp,
    onClick: () -> Unit = { },
    icon: ImageVector,
    text: String? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .wrapContentHeight()
            .padding(start = paddingStart, top = 16.dp, end = 8.dp)
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .height(48.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            text?.let {
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = text,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            content.invoke()
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun SettingMainPagePreview() {
    SettingMainPage(uiState = SettingUiState())
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun AuthenticationPagePreview() {
    AuthenticationPage(navController = rememberNavController())
}