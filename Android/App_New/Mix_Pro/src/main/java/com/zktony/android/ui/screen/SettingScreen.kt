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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
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
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.viewmodel.SettingPage
import com.zktony.android.ui.viewmodel.SettingUiState
import com.zktony.android.ui.viewmodel.SettingViewModel
import com.zktony.core.ext.Ext
import com.zktony.core.ext.createQRCodeBitmap
import com.zktony.core.model.QrCode
import kotlinx.coroutines.delay

/**
 * Setting screen
 *
 * @param modifier Modifier
 * @param viewModel SettingViewModel
 * @param navController NavHostController
 */
@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel,
    navController: NavHostController,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnimatedVisibility(visible = uiState.page == SettingPage.SETTING) {
        SettingPage(
            modifier = modifier,
            navController = navController,
            navigationTo = viewModel::navigateTo,
            uiState = uiState,
            setNavigation = viewModel::setNavigation,
            setLanguage = viewModel::setLanguage,
            openWifi = viewModel::openWifi,
            checkUpdate = viewModel::checkUpdate,
        )
    }

    AnimatedVisibility(visible = uiState.page == SettingPage.AUTHENTICATION) {
        AuthenticationPage(
            modifier = modifier,
            navController = navController,
            navigationTo = viewModel::navigateTo,
        )
    }
}

/**
 * Setting page
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param navigationTo Function1<SettingPage, Unit>
 * @param uiState SettingUiState
 * @param setNavigation Function1<Boolean, Unit>
 * @param setLanguage Function1<String, Unit>
 * @param openWifi Function0<Unit>
 * @param checkUpdate Function0<Unit>
 */
@Composable
fun SettingPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    navigationTo: (SettingPage) -> Unit = {},
    uiState: SettingUiState,
    setNavigation: (Boolean) -> Unit = {},
    setLanguage: (String) -> Unit = {},
    openWifi: () -> Unit = {},
    checkUpdate: () -> Unit = {},
) {
    BackHandler {
        navController.popBackStack()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f),
        ) {
            SettingsForm(
                modifier = Modifier.weight(1f),
                uiState = uiState,
                setNavigation = setNavigation,
                setLanguage = setLanguage,
            )
            InfoForm(
                modifier = Modifier.weight(1f),
            )
        }
        OperationForm(
            modifier = Modifier.wrapContentHeight(),
            uiState = uiState,
            openWifi = openWifi,
            checkUpdate = checkUpdate,
            navigationTo = navigationTo,
        )
    }
}

/**
 * Authentication page
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param navigationTo Function1<SettingPage, Unit>
 */
@Composable
fun AuthenticationPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    navigationTo: (SettingPage) -> Unit = {},
) {
    BackHandler {
        navigationTo(SettingPage.SETTING)
    }

    var show by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(8.dp)
            .background(
                MaterialTheme.colorScheme.background,
                MaterialTheme.shapes.medium,
            )
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ZkTonyTopAppBar(
            title = "",
            onBack = {
                navigationTo(SettingPage.SETTING)
            }
        )
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
                Card(
                    modifier = Modifier.clickable {
                        navigationTo(SettingPage.SETTING)
                        navController.navigate(Route.MOTOR)
                    },
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 64.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            modifier = Modifier.size(96.dp),
                            painter = painterResource(id = R.drawable.ic_motor),
                            contentDescription = stringResource(id = R.string.motor_config)
                        )
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp),
                            text = stringResource(id = R.string.motor_config),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Card(
                    modifier = Modifier.clickable {
                        navigationTo(SettingPage.SETTING)
                        navController.navigate(Route.CONFIG)
                    },
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 64.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            modifier = Modifier.size(96.dp),
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = stringResource(id = R.string.system_config)
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

/**
 * Settings form
 *
 * @param modifier Modifier
 * @param uiState SettingUiState
 * @param setNavigation Function1<Boolean, Unit>
 * @param setLanguage Function1<String, Unit>
 */
@Composable
fun SettingsForm(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    setNavigation: (Boolean) -> Unit = {},
    setLanguage: (String) -> Unit = {},
) {
    val lazyColumnState = rememberLazyListState()
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
            Card(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(start = 8.dp, top = 16.dp, end = 8.dp)
                    .clickable { expanded = !expanded },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(start = 16.dp),
                        painter = painterResource(id = R.drawable.ic_language),
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(id = R.string.language),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier.padding(end = 16.dp),
                        text = when (uiState.language) {
                            "en" -> "English"
                            "zh" -> "简体中文"
                            else -> "English"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
        languageList.forEach { (name, code) ->
            item {
                AnimatedVisibility(visible = expanded) {
                    Card(
                        modifier = Modifier
                            .wrapContentHeight()
                            .padding(start = 32.dp, top = 16.dp, end = 8.dp)
                            .clickable {
                                setLanguage(code)
                                expanded = false
                            },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(start = 16.dp),
                                painter = painterResource(id = R.drawable.ic_language),
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                modifier = Modifier.padding(end = 16.dp),
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(start = 8.dp, top = 16.dp, end = 8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(start = 16.dp),
                        painter = painterResource(id = R.drawable.ic_navigation),
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(id = R.string.navigation),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    Switch(
                        modifier = Modifier.padding(end = 16.dp),
                        checked = uiState.navigation,
                        onCheckedChange = { setNavigation(it) },
                    )
                }
            }
        }
    }
}

/**
 * Info form
 *
 * @param modifier Modifier
 */
@SuppressLint("HardwareIds")
@Composable
fun InfoForm(
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
                Card(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(start = 8.dp, top = 16.dp, end = 8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(start = 16.dp),
                            painter = painterResource(id = R.drawable.ic_version),
                            contentDescription = null,
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = stringResource(id = R.string.version),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            modifier = Modifier.padding(end = 16.dp),
                            text = BuildConfig.VERSION_NAME,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                        )
                    }
                }
            }

        }
        item {
            AnimatedVisibility(visible = !deviceInfo && !helpInfo) {
                Card(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(start = 8.dp, top = 16.dp, end = 8.dp)
                        .clickable { deviceInfo = !deviceInfo },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(start = 16.dp),
                            painter = painterResource(id = R.drawable.ic_device),
                            contentDescription = null,
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = stringResource(id = R.string.device_info),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            modifier = Modifier
                                .size(36.dp)
                                .padding(end = 16.dp),
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = stringResource(id = R.string.device_info)
                        )
                    }
                }
            }

        }
        item {
            AnimatedVisibility(visible = !deviceInfo && !helpInfo) {
                Card(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(start = 8.dp, top = 16.dp, end = 8.dp)
                        .clickable { helpInfo = !helpInfo },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(start = 16.dp),
                            painter = painterResource(id = R.drawable.ic_help),
                            contentDescription = null,
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = stringResource(id = R.string.help),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            modifier = Modifier
                                .size(36.dp)
                                .padding(end = 16.dp),
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = stringResource(id = R.string.help)
                        )
                    }
                }
            }
        }
        item {
            AnimatedVisibility(visible = deviceInfo) {
                Card(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(start = 8.dp, top = 16.dp, end = 8.dp)
                        .clickable { deviceInfo = !deviceInfo },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.padding(start = 16.dp),
                            text = stringResource(id = R.string.device_info),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(end = 16.dp),
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.device_info),
                        )
                    }
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
                Card(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(start = 8.dp, top = 16.dp, end = 8.dp)
                        .clickable { helpInfo = !helpInfo },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.padding(start = 16.dp),
                            text = stringResource(id = R.string.qrcode),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(end = 16.dp),
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.qrcode),
                        )
                    }
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

/**
 * Operation form
 *
 * @param modifier Modifier
 * @param uiState SettingUiState
 * @param openWifi Function0<Unit>
 * @param checkUpdate Function0<Unit>
 * @param navigationTo Function1<SettingPage, Unit>
 */
@Composable
fun OperationForm(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    openWifi: () -> Unit = {},
    checkUpdate: () -> Unit = {},
    navigationTo: (SettingPage) -> Unit = {},
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
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp, vertical = 8.dp)
                .clickable { navigationTo(SettingPage.AUTHENTICATION) },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier.size(96.dp),
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = stringResource(id = R.string.parameters)
                )
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = stringResource(id = R.string.parameters),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp, vertical = 8.dp)
                .clickable { openWifi() },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier.size(96.dp),
                    painter = painterResource(id = R.drawable.ic_wifi),
                    contentDescription = stringResource(id = R.string.wifi)
                )
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = stringResource(id = R.string.wifi),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        Card(
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
                    R.drawable.ic_update
                } else {
                    if (uiState.application.versionCode > BuildConfig.VERSION_CODE) {
                        R.drawable.ic_update_avialable
                    } else {
                        R.drawable.ic_good
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
                    Image(
                        modifier = Modifier.size(96.dp),
                        painter = painterResource(id = icon),
                        contentDescription = text,
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

/**
 * Code item
 *
 * @param text String
 * @param focused Boolean
 */
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

/**
 * Verification code field
 *
 * @param digits Int
 * @param horizontalMargin Dp
 * @param inputCallback Function1<[@kotlin.ParameterName] String, Unit>
 * @param itemScope [@androidx.compose.runtime.Composable] Function2<[@kotlin.ParameterName] String, [@kotlin.ParameterName] Boolean, Unit>
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VerificationCodeField(
    digits: Int,
    horizontalMargin: Dp = 16.dp,
    inputCallback: (content: String) -> Unit = {},
    itemScope: @Composable (text: String, focused: Boolean) -> Unit
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
            modifier = Modifier.padding(vertical = 16.dp),
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
@Preview(showBackground = true)
fun SettingsFormPreview() {
    SettingsForm(
        uiState = SettingUiState(),
    )
}

@Composable
@Preview(showBackground = true)
fun InfoFormPreview() {
    InfoForm()
}

@Composable
@Preview(showBackground = true, widthDp = 960)
fun OperationFormPreview() {
    OperationForm(
        uiState = SettingUiState(),
        openWifi = {},
        checkUpdate = {},
    )
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun SettingPagePreview() {
    SettingPage(
        navController = rememberNavController(),
        uiState = SettingUiState(),
    )
}


@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun AuthenticationPagePreview() {
    AuthenticationPage(navController = rememberNavController())
}