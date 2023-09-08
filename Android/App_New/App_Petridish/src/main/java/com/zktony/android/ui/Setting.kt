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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
import com.zktony.android.ui.components.Header
import com.zktony.android.ui.components.VerificationCodeField
import com.zktony.android.ui.components.VerificationCodeItem
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.Constants
import com.zktony.android.utils.ext.format
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToLong

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
            PageType.SETTINGS -> navController.navigateUp()
            PageType.MOTOR_DETAIL -> viewModel.event(SettingEvent.NavTo(PageType.MOTOR_LIST))
            else -> viewModel.event(SettingEvent.NavTo(PageType.SETTINGS))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(visible = uiState.page != PageType.SETTINGS) {
            Header(
                onBackPressed = {
                    when (uiState.page) {
                        PageType.MOTOR_DETAIL -> viewModel.event(SettingEvent.NavTo(PageType.MOTOR_LIST))
                        else -> viewModel.event(SettingEvent.NavTo(PageType.SETTINGS))
                    }
                }
            ) {
                Image(
                    modifier = Modifier.size(36.dp),
                    painter = painterResource(id = R.drawable.ic_setting),
                    contentDescription = null,
                )
            }
        }
        AnimatedVisibility(visible = uiState.page == PageType.SETTINGS) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Display the settings content
                    SettingsContent(
                        modifier = Modifier.weight(1f),
                        event = viewModel::event,
                    )
                    // Display the info content
                    InfoContent(
                        modifier = Modifier.weight(1f),
                    )
                }
                // Display the operation content
                OperationContent(
                    uiState = uiState,
                    event = viewModel::event,
                )
            }
        }
        AnimatedVisibility(visible = uiState.page == PageType.AUTH) {
            Authentication(modifier = modifier, event = viewModel::event)
        }
        AnimatedVisibility(visible = uiState.page == PageType.MOTOR_LIST) {
            MotorList(
                modifier = modifier,
                uiState = uiState,
                event = viewModel::event,
            )
        }
        AnimatedVisibility(visible = uiState.page == PageType.MOTOR_DETAIL) {
            MotorDetail(
                modifier = modifier,
                uiState = uiState,
                event = viewModel::event,
            )
        }
        AnimatedVisibility(visible = uiState.page == PageType.CONFIG) {
            ConfigList(modifier = modifier, event = viewModel::event)
        }
    }
}

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    event: (SettingEvent) -> Unit = {},
) {
    var navigation by rememberDataSaverState(
        key = Constants.NAVIGATION,
        default = false
    )

    // Define the lazy column state and coroutine scope
    val lazyColumnState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Display the settings content
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
            ),
        state = lazyColumnState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Display the navigation setting
        item {
            SettingsCard(
                image = R.drawable.ic_navigation,
                text = stringResource(id = R.string.navigation),
            ) {
                Switch(
                    modifier = Modifier.height(32.dp),
                    checked = navigation,
                    onCheckedChange = {
                        scope.launch {
                            navigation = it
                            event(SettingEvent.Navigation(it))
                        }
                    },
                )
            }
        }
    }
}

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
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
            ),
        state = lazyColumnState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Display the version info
        item {
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
        // Display the help info toggle
        item {
            SettingsCard(
                image = R.drawable.ic_help,
                text = if (helpInfo) stringResource(id = R.string.qrcode) else stringResource(id = R.string.help),
                onClick = { helpInfo = !helpInfo },
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = if (helpInfo) Icons.Default.Close else Icons.Default.ArrowForward,
                    contentDescription = null,
                )
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
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Display the parameters card
        ElevatedCard(onClick = { event(SettingEvent.NavTo(PageType.AUTH)) }) {
            Column(
                modifier = Modifier.padding(horizontal = 64.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier.size(96.dp),
                    painter = painterResource(id = R.drawable.ic_setting),
                    contentDescription = null,
                )
                Text(
                    text = stringResource(id = R.string.parameters),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        // Display the network card
        ElevatedCard(
            onClick = { event(SettingEvent.Network) }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 64.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier.size(96.dp),
                    painter = painterResource(id = R.drawable.ic_wifi),
                    contentDescription = null,
                )
                Text(
                    text = stringResource(id = R.string.wifi),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        // Display the update card
        ElevatedCard(
            onClick = { event(SettingEvent.CheckUpdate) }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 64.dp, vertical = 8.dp),
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
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Authentication(
    modifier: Modifier = Modifier,
    event: (SettingEvent) -> Unit = {},
) {
    var show by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
                ElevatedCard(onClick = { event(SettingEvent.NavTo(PageType.MOTOR_LIST)) }) {
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
                ElevatedCard(onClick = { event(SettingEvent.NavTo(PageType.CONFIG)) }) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCard(
    paddingStart: Dp = 0.dp,
    onClick: () -> Unit = { },
    image: Int,
    text: String? = null,
    content: @Composable () -> Unit,
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
            // Display the image in the settings card
            Image(
                modifier = Modifier.size(32.dp),
                painter = painterResource(id = image),
                contentDescription = null,
            )
            // Display the text in the settings card
            text?.let {
                Text(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorList(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    event: (SettingEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
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
                        event(SettingEvent.ToggleSelected(it.id)) // Step 1: Toggle the selected state of the entity
                        event(SettingEvent.NavTo(PageType.MOTOR_DETAIL)) // Step 2: Navigate to the edit page
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
    event: (SettingEvent) -> Unit = {},
) {
    // Get the selected entity from the UI state
    val entity = uiState.entities.find { it.id == uiState.selected }!!

    // Define the state variables for speed, acceleration, and deceleration
    var speed by remember { mutableStateOf(entity.speed) }
    var acc by remember { mutableStateOf(entity.acc) }
    var dec by remember { mutableStateOf(entity.dec) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
            ),
        verticalArrangement = Arrangement.Center,
    ) {
        // Speed slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(36.dp),
                painter = painterResource(id = R.drawable.ic_speed),
                contentDescription = stringResource(id = R.string.speed)
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

        // Acceleration slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(36.dp),
                painter = painterResource(id = R.drawable.ic_rocket),
                contentDescription = stringResource(id = R.string.acceleration)
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

        // Deceleration slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(36.dp),
                painter = painterResource(id = R.drawable.ic_turtle),
                contentDescription = stringResource(id = R.string.deceleration)
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

        // Show the update button if any of the values have changed
        AnimatedVisibility(visible = entity.speed != speed || entity.acc != acc || entity.dec != dec) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                FloatingActionButton(
                    modifier = Modifier.width(192.dp),
                    onClick = {
                        // Update the entity with the new values and navigate back to the list page
                        event(SettingEvent.Update(entity.copy(speed = speed, acc = acc, dec = dec)))
                        event(SettingEvent.NavTo(PageType.MOTOR_LIST))
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


@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun ConfigList(
    modifier: Modifier = Modifier,
    event: (SettingEvent) -> Unit = { },
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
            )
            .windowInsetsPadding(WindowInsets.imeAnimationSource),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            val x = rememberDataSaverState(key = Constants.MAX_X, default = 0f)
            val y = rememberDataSaverState(key = Constants.MAX_Y, default = 0f)
            var ex by remember { mutableStateOf(x.value.format(1)) }
            var ey by remember { mutableStateOf(y.value.format(1)) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "最大行程",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = ex,
                        onValueChange = {
                            scope.launch {
                                ex = it
                                x.value = it.toFloatOrNull() ?: 0f
                            }
                        },
                        label = { Text(text = "横坐标") },
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
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            modifier = Modifier.width(96.dp),
                            onClick = {
                                scope.launch {
                                    keyboard?.hide()
                                    event(SettingEvent.MoveTo(0, x.value))
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = ey,
                        onValueChange = {
                            scope.launch {
                                ey = it
                                y.value = it.toFloatOrNull() ?: 0f
                            }
                        },
                        label = { Text(text = "纵坐标") },
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
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            modifier = Modifier.width(96.dp),
                            onClick = {
                                scope.launch {
                                    keyboard?.hide()
                                    event(SettingEvent.MoveTo(1, y.value))
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null
                            )
                        }
                    }
                }
            }

        }
        item {
            val x = rememberDataSaverState(key = Constants.WASH_TANK_X, default = 0f)
            val y = rememberDataSaverState(key = Constants.WASH_TANK_Y, default = 0f)
            var ex by remember { mutableStateOf(x.value.format(1)) }
            var ey by remember { mutableStateOf(y.value.format(1)) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "废液槽坐标",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = ex,
                        onValueChange = {
                            scope.launch {
                                ex = it
                                x.value = it.toFloatOrNull() ?: 0f
                            }
                        },
                        label = { Text(text = "横坐标") },
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
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            modifier = Modifier.width(96.dp),
                            onClick = {
                                scope.launch {
                                    keyboard?.hide()
                                    event(SettingEvent.MoveTo(0, x.value))
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = ey,
                        onValueChange = {
                            scope.launch {
                                ey = it
                                y.value = it.toFloatOrNull() ?: 0f
                            }
                        },
                        label = { Text(text = "纵坐标") },
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
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            modifier = Modifier.width(96.dp),
                            onClick = {
                                scope.launch {
                                    keyboard?.hide()
                                    event(SettingEvent.MoveTo(1, y.value))
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null
                            )
                        }
                    }
                }
            }

        }
    }
}


@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun SettingPreview() {
    // Create a new instance of the setting UI state
    val uiState = SettingUiState()

    // Display the content wrapper for the setting list
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Display the settings content
            SettingsContent(
                modifier = Modifier.weight(1f),
            )
            // Display the info content
            InfoContent(
                modifier = Modifier.weight(1f),
            )
        }
        // Display the operation content
        OperationContent(
            uiState = uiState,
        )
    }

}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun AuthenticationPreview() {
    Authentication()
}