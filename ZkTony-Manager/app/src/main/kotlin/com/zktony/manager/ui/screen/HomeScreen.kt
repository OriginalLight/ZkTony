/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zktony.manager.ui.screen

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.DisplayFeature
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.google.gson.Gson
import com.zktony.manager.R
import com.zktony.manager.data.remote.model.Software
import com.zktony.manager.ui.QrCodeActivity
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.manager.ui.components.ManagerCheckAppBar
import com.zktony.manager.ui.utils.ContentType
import kotlinx.coroutines.delay

// region HomeScreen
@Composable
fun HomeScreen(
    contentType: ContentType,
    displayFeatures: List<DisplayFeature>,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (contentType == ContentType.SINGLE_PANE) {
        HomeScreenSinglePane(
            modifier = modifier,
            uiState = uiState,
            viewModel = viewModel
        )
    } else {
        HomeScreenDualPane(
            modifier = modifier,
            uiState = uiState,
            viewModel = viewModel,
            displayFeatures = displayFeatures,
        )
    }

}
// endregion

// region HomeScreenSinglePane
@Composable
fun HomeScreenSinglePane(
    modifier: Modifier,
    uiState: HomeUiState,
    viewModel: HomeViewModel,
) {
    AnimatedVisibility(
        visible = (uiState.page == HomePage.HOME),
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        HomePageContent(
            modifier = modifier,
            viewModel = viewModel,
        )
    }
    AnimatedVisibility(
        visible = (uiState.page == HomePage.SHIPPING),
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        ShippingPageContent(
            modifier = modifier,
            uiState = uiState.shipping,
            viewModel = viewModel,
        )
    }
}
// endregion

// region HomeScreenDualPane
@Composable
fun HomeScreenDualPane(
    modifier: Modifier,
    displayFeatures: List<DisplayFeature>,
    uiState: HomeUiState,
    viewModel: HomeViewModel
) {
    TwoPane(
        first = {
            HomePageContent(
                modifier = modifier,
                viewModel = viewModel,
            )
        },
        second = { /*TODO*/ },
        strategy = HorizontalTwoPaneStrategy(splitFraction = 0.5f, gapWidth = 16.dp),
        displayFeatures = displayFeatures
    )
}
// endregion

// region HomePageContent
@Composable
fun HomePageContent(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
) {

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 64.dp, bottom = 16.dp),
        text = stringResource(id = R.string.screen_home_title),
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
    )

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HomePageCard(
            title = stringResource(id = R.string.page_shipping_title),
            subtitle = stringResource(id = R.string.page_shipping_subtitle),
            icon = Icons.Outlined.LocalShipping,
            onClick = { viewModel.navigateTo(HomePage.SHIPPING) }
        )
        HomePageCard(
            title = stringResource(id = R.string.page_shipping_history_title),
            subtitle = stringResource(id = R.string.page_shipping_history_subtitle),
            icon = Icons.Outlined.History,
            onClick = { viewModel.navigateTo(HomePage.SHIPPING_HISTORY) }
        )
        HomePageCard(
            title = stringResource(id = R.string.page_after_sale_title),
            subtitle = stringResource(id = R.string.page_after_sale_subtitle),
            icon = Icons.Outlined.Shop,
            onClick = { viewModel.navigateTo(HomePage.AFTER_SALE) }
        )
        HomePageCard(
            title = stringResource(id = R.string.page_after_sale_history_title),
            subtitle = stringResource(id = R.string.page_after_sale_history_subtitle),
            icon = Icons.Outlined.History,
            onClick = { viewModel.navigateTo(HomePage.AFTER_SALE_HISTORY) }
        )
    }
}
// endregion

// region HomePageCard
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            focusedElevation = 4.dp,
            disabledElevation = 0.dp,
        ),
        shape = MaterialTheme.shapes.medium,
        onClick = { onClick() }
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontStyle = MaterialTheme.typography.labelLarge.fontStyle,
                    fontFamily = FontFamily.SansSerif,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }

    }
}
// endregion

// region ModifyPageContent
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ModifyPageContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    navigateTo: (HomePage) -> Unit = {},
    onDone: () -> Unit = {},
    value: String = "",
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var textValue by remember { mutableStateOf(value) }

    BackHandler {
        navigateTo(HomePage.SHIPPING)
    }

    LaunchedEffect(Lifecycle.State.STARTED) {
        delay(300)
        focusRequester.requestFocus().also {
            keyboardController?.show()
        }
    }

    Column {
        ManagerCheckAppBar(
            modifier = modifier,
            title = "Modify",
            onBack = { navigateTo(HomePage.SHIPPING) },
            onDone = { onDone() },
            isFullScreen = true
        )
        TextField(
            value = textValue,
            onValueChange = { textValue = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(4.dp, CircleShape)
                .focusRequester(focusRequester),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = MaterialTheme.colorScheme.surface,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.surface,
                textColor = MaterialTheme.colorScheme.onSurface,
                placeholderColor = MaterialTheme.colorScheme.outline,
                containerColor = MaterialTheme.colorScheme.surface
            ),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontStyle = MaterialTheme.typography.bodyMedium.fontStyle,
            ),
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
                localFocusManager.clearFocus()
                onDone()
            }),
            visualTransformation = VisualTransformation.None,
        )
    }

}
// endregion

// region ShippingPageContent
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ShippingPageContent(
    modifier: Modifier = Modifier,
    uiState: ShippingState,
    viewModel: HomeViewModel
) {
    BackHandler {
        viewModel.navigateTo(HomePage.HOME)
    }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val qrCodeScanner =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val result = it.data?.getStringExtra("SCAN_RESULT")
                // result 是json字符串解析成software对象
                val software = Gson().fromJson(result, Software::class.java)
                viewModel.setSoftware(software)
            }
        }

    Column {
        ManagerAppBar(
            title = stringResource(id = R.string.page_shipping_title),
            onBack = { viewModel.navigateTo(HomePage.HOME) },
            isFullScreen = true
        )

        TextField(
            value = uiState.software.id,
            label = { Text(text = "Android ID") },
            onValueChange = { viewModel.setSoftware(uiState.software.copy(id = it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .focusRequester(focusRequester),
            trailingIcon = {
                Icon(
                    modifier = Modifier
                        .size(36.dp)
                        .absoluteOffset(x = (-8).dp)
                        .clickable {
                            qrCodeScanner.launch(
                                Intent(
                                    context,
                                    QrCodeActivity::class.java
                                )
                            )
                        },
                    imageVector = Icons.Outlined.QrCode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colorScheme.onSurface,
                placeholderColor = MaterialTheme.colorScheme.outline,
                containerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                fontStyle = MaterialTheme.typography.bodyLarge.fontStyle,
            ),
            maxLines = 1,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
                localFocusManager.clearFocus()
            }),
            visualTransformation = VisualTransformation.None,
        )

    }

}
// endregion

// region Preview
@Preview
@Composable
fun HomePageContentPreview() {
    HomePageContent(
        viewModel = HomeViewModel()
    )
}

@Preview
@Composable
fun HomePageCardPreview() {
    HomePageCard(
        title = "Title",
        subtitle = "Subtitle",
        icon = Icons.Outlined.LocalShipping,
        onClick = {}
    )
}

@Preview
@Composable
fun ModifyPageContentPreview() {
    ModifyPageContent(
        uiState = HomeUiState(),
        navigateTo = {},
        onDone = {},
        value = "Value"
    )
}

@Preview
@Composable
fun ShippingPageContentPreview() {
    ShippingPageContent(
        uiState = ShippingState(),
        viewModel = HomeViewModel()
    )
}
// endregion
