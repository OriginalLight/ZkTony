package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.ui.components.TopAppBar
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Composable function that displays the settings screen.
 *
 * @param modifier The modifier to apply to the composable.
 * @param navController The NavHostController used for navigation.
 * @param viewModel The view model used for the settings screen.
 */
@Composable
fun Setting(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: SettingViewModel = koinViewModel(),
) {
    // Collect the UI state from the view model
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle the back button press
    BackHandler {
        when (uiState.page) {
            PageType.LIST -> navController.navigateUp()
            else -> viewModel.event(SettingEvent.NavTo(PageType.LIST))
        }
    }

    // Display the content wrapper
    ContentWrapper(
        modifier = modifier,
        uiState = uiState,
        event = viewModel::event,
        navController = navController,
    )
}

/**
 * Composable function that displays the content wrapper for the settings screen.
 *
 * @param modifier The modifier to apply to the composable.
 * @param uiState The UI state for the settings screen.
 * @param event The event handler for the settings screen.
 * @param navController The NavHostController used for navigation.
 */
@Composable
fun ContentWrapper(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    event: (SettingEvent) -> Unit = {},
    navController: NavHostController,
) {
    Column(modifier = modifier) {
        // Display the top app bar when the authentication page is visible
        AnimatedVisibility(visible = uiState.page == PageType.AUTH) {
            TopAppBar(
                title = stringResource(id = R.string.authentication),
                navigation = { event(SettingEvent.NavTo(PageType.LIST)) }
            )
        }
        // Display the main page
        AnimatedVisibility(visible = uiState.page == PageType.LIST) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Display the settings content
                    SettingsContent(
                        modifier = Modifier.weight(1f),
                        uiState = uiState,
                        event = event,
                    )
                    // Display the info content
                    InfoContent(
                        modifier = Modifier.weight(1f),
                    )
                }
                // Display the operation content
                OperationContent(
                    modifier = Modifier.wrapContentHeight(),
                    uiState = uiState,
                    event = event,
                )
            }
        }
        // Display the authentication page
        AnimatedVisibility(visible = uiState.page == PageType.AUTH) {
            Authentication(
                modifier = modifier,
                event = event,
                navController = navController,
            )
        }
    }
}

/**
 * Composable function that displays the settings content.
 *
 * @param modifier The modifier to apply to the composable.
 * @param uiState The UI state for the settings screen.
 * @param event The event handler for the settings screen.
 */
@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    event: (SettingEvent) -> Unit = {},
) {
    // Define the language list
    val languageList = listOf(Pair("English", "en"), Pair("简体中文", "zh"))

    // Define the lazy column state and coroutine scope
    val lazyColumnState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Define the expanded state for the language list
    var expanded by remember { mutableStateOf(false) }

    // Display the settings content
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .animateContentSize()
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium,
            ),
        state = lazyColumnState,
    ) {
        // Display the language setting
        item {
            SettingsCard(
                image = R.drawable.ic_language,
                text = stringResource(id = R.string.language),
                onClick = { expanded = !expanded }
            ) {
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
        // Display the language list
        languageList.forEach { (name, code) ->
            item {
                AnimatedVisibility(visible = expanded) {
                    SettingsCard(
                        image = R.drawable.ic_language,
                        paddingStart = 32.dp,
                        onClick = {
                            scope.launch {
                                event(SettingEvent.Language(code))
                                expanded = false
                            }
                        }
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
        // Display the navigation setting
        item {
            SettingsCard(
                image = R.drawable.ic_navigation,
                text = stringResource(id = R.string.navigation),
            ) {
                Switch(
                    checked = uiState.settings.navigation,
                    onCheckedChange = {
                        scope.launch {
                            event(SettingEvent.Navigation(it))
                        }
                    },
                )
            }
        }
    }
}

/**
 * Composable function that displays the info content.
 *
 * @param modifier The modifier to apply to the composable.
 */
@Composable
fun InfoContent(
    modifier: Modifier = Modifier,
) {
    // Define the lazy column state and expanded state for the help info
    val lazyColumnState = rememberLazyListState()
    var helpInfo by remember { mutableStateOf(false) }

    // Display the info content
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .animateContentSize()
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        state = lazyColumnState,
    ) {
        // Display the version info
        item {
            AnimatedVisibility(visible = !helpInfo) {
                SettingsCard(
                    image = R.drawable.ic_version,
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
        // Display the help info toggle
        item {
            AnimatedVisibility(visible = !helpInfo) {
                SettingsCard(
                    image = R.drawable.ic_help,
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
        // Display the QR code help info
        item {
            AnimatedVisibility(visible = helpInfo) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    SettingsCard(
                        image = R.drawable.ic_help,
                        text = stringResource(id = R.string.qrcode),
                        onClick = { helpInfo = !helpInfo },
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                        )
                    }
                    Card(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Image(
                                modifier = Modifier.size(200.dp),
                                painter = painterResource(id = R.mipmap.qrcode),
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
 * Composable function that displays the operation content.
 *
 * @param modifier The modifier to apply to the composable.
 * @param uiState The UI state for the settings screen.
 * @param event The event handler for the settings screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationContent(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    event: (SettingEvent) -> Unit = {},
) {
    // Display the operation content
    Row(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium,
            ),
    ) {
        // Display the parameters card
        ElevatedCard(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp, vertical = 8.dp),
            onClick = { event(SettingEvent.NavTo(PageType.AUTH)) }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier.size(96.dp),
                    painter = painterResource(id = R.drawable.ic_setting),
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = stringResource(id = R.string.parameters),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        // Display the network card
        ElevatedCard(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp, vertical = 8.dp),
            onClick = { event(SettingEvent.Network) }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier.size(96.dp),
                    painter = painterResource(id = R.drawable.ic_wifi),
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = stringResource(id = R.string.wifi),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        // Display the update card
        ElevatedCard(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp, vertical = 8.dp),
            onClick = { event(SettingEvent.Update) }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Determine the icon and text to display based on the UI state
                val painter = if (uiState.application == null) {
                    painterResource(id = R.drawable.ic_sync)
                } else {
                    if (uiState.application.versionCode > BuildConfig.VERSION_CODE) {
                        painterResource(id = R.drawable.ic_new)
                    } else {
                        painterResource(id = R.drawable.ic_happy_cloud)
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
                // Display the progress indicator or the icon and text
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
                        painter = painter,
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
 * Composable function that displays a verification code item.
 *
 * @param text The text to display in the verification code item.
 * @param focused Whether the verification code item is focused.
 */
@Composable
fun VerificationCodeItem(text: String, focused: Boolean) {
    // Determine the border color based on the focus state
    val borderColor = if (focused) {
        MaterialTheme.colorScheme.onBackground
    } else {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
    }

    // Display the verification code item
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
 * Composable function that displays a verification code field.
 *
 * @param digits The number of digits in the verification code field.
 * @param horizontalMargin The horizontal margin to apply between verification code items.
 * @param inputCallback The callback to invoke when the verification code is entered.
 * @param itemScope The composable function to use to display each verification code item.
 */
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

    // Delay the focus request and keyboard show to avoid race conditions
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Box {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display each verification code item
            repeat(digits) {
                if (it != 0) {
                    // Add horizontal margin between verification code items
                    Spacer(modifier = Modifier.width(horizontalMargin))
                }
                // Determine the text to display in the verification code item
                val text = if (content.getOrNull(it) != null) "*" else ""
                // Determine whether the verification code item is focused
                val focused = it == content.length
                // Display the verification code item
                itemScope(text, focused)
            }
        }
        // Display the text field for entering the verification code
        BasicTextField(
            modifier = Modifier
                .focusRequester(focusRequester)
                .drawWithContent { }// Clear the content to avoid overlapping with the verification code items
                .matchParentSize(),
            value = content,
            onValueChange = {
                content = it
                if (it.length == digits) {
                    if (it == "123456") {
                        // Invoke the input callback when the verification code is entered
                        inputCallback(it)
                        keyboardController?.hide()
                    } else {
                        // Clear the verification code field and request focus again if the verification code is incorrect
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

/**
 * Composable function that displays the authentication screen.
 *
 * @param modifier The modifier to apply to the composable.
 * @param event The event handler for the authentication screen.
 * @param navController The navigation controller for the authentication screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Authentication(
    modifier: Modifier = Modifier,
    event: (SettingEvent) -> Unit = {},
    navController: NavHostController,
) {
    var show by remember { mutableStateOf(false) }

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
        // Display the authentication header
        Spacer(modifier = Modifier.height(128.dp))
        // Display the verification code field
        AnimatedVisibility(visible = !show) {
            VerificationCodeField(digits = 6, inputCallback = {
                show = true
            }) { text, focused ->
                VerificationCodeItem(text, focused)
            }
        }
        // Display the navigation buttons
        AnimatedVisibility(visible = show) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Display the motor configuration button
                ElevatedCard(
                    onClick = {
                        event(SettingEvent.NavTo(PageType.LIST))
                        navController.navigate(Route.MOTOR)
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 64.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            modifier = Modifier.size(96.dp),
                            painter = painterResource(id = R.drawable.ic_engine),
                            contentDescription = null,
                        )
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp),
                            text = stringResource(id = R.string.motor_config),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                // Display the system configuration button
                ElevatedCard(
                    onClick = {
                        event(SettingEvent.NavTo(PageType.LIST))
                        navController.navigate(Route.CONFIG)
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 64.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            modifier = Modifier.size(96.dp),
                            painter = painterResource(id = R.drawable.ic_config),
                            contentDescription = null,
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
 * Composable function that displays a settings card.
 *
 * @param paddingStart The start padding to apply to the settings card.
 * @param onClick The click listener for the settings card.
 * @param image The image resource ID to display in the settings card.
 * @param text The text to display in the settings card.
 * @param content The composable function to use to display the content of the settings card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCard(
    paddingStart: Dp = 8.dp,
    onClick: () -> Unit = { },
    image: Int,
    text: String? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .wrapContentHeight()
            .padding(start = paddingStart, top = 16.dp, end = 8.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .height(48.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Display the image in the settings card
            Image(
                modifier = Modifier.size(32.dp),
                painter = painterResource(id = image),
                contentDescription = null,
            )
            // Display the text in the settings card
            text?.let {
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = text,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            // Display the content of the settings card
            Spacer(modifier = Modifier.weight(1f))
            content.invoke()
        }
    }
}

/**
 * Composable function that displays a preview of the setting list.
 */
@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun SettingListPreview() {
    // Create a new instance of the setting UI state
    val uiState = SettingUiState()
    // Create a new instance of the navigation controller
    val navController = rememberNavController()

    // Display the content wrapper for the setting list
    ContentWrapper(uiState = uiState, navController = navController)
}

/**
 * Composable function that displays a preview of the authentication screen.
 */
@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun AuthenticationPreview() {
    // Create a new instance of the navigation controller
    val navController = rememberNavController()

    // Display the authentication screen
    Authentication(navController = navController)
}