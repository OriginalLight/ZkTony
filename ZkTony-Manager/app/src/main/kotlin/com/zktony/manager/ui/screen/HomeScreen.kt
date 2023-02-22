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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.DisplayFeature
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.zktony.manager.R
import com.zktony.manager.ui.components.FunctionCard
import com.zktony.manager.ui.components.ManagerCheckAppBar
import com.zktony.manager.ui.components.QrCodeTextField
import com.zktony.manager.ui.components.SoftwareCard
import com.zktony.manager.ui.utils.ContentType
import kotlinx.coroutines.delay

// region HomeScreen
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    contentType: ContentType,
    displayFeatures: List<DisplayFeature>,
    viewModel: HomeViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (contentType == ContentType.SINGLE_PANE) {
        HomeScreenSinglePane(
            modifier = modifier, uiState = uiState, viewModel = viewModel
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageContent(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.screen_home_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
            )
        },
        content = { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FunctionCard(
                    title = stringResource(id = R.string.page_shipping_title),
                    subtitle = stringResource(id = R.string.page_shipping_subtitle),
                    icon = Icons.Outlined.LocalShipping,
                    onClick = { viewModel.navigateTo(HomePage.SHIPPING) },
                    shape = RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp
                    ),
                )
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp), color = Color.Gray
                )
                FunctionCard(title = stringResource(id = R.string.page_shipping_history_title),
                    subtitle = stringResource(id = R.string.page_shipping_history_subtitle),
                    icon = Icons.Outlined.History,
                    onClick = { viewModel.navigateTo(HomePage.SHIPPING_HISTORY) })
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp), color = Color.Gray
                )
                FunctionCard(title = stringResource(id = R.string.page_after_sale_title),
                    subtitle = stringResource(id = R.string.page_after_sale_subtitle),
                    icon = Icons.Outlined.Shop,
                    onClick = { viewModel.navigateTo(HomePage.AFTER_SALE) })
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp), color = Color.Gray
                )
                FunctionCard(
                    title = stringResource(id = R.string.page_after_sale_history_title),
                    subtitle = stringResource(id = R.string.page_after_sale_history_subtitle),
                    icon = Icons.Outlined.History,
                    onClick = { viewModel.navigateTo(HomePage.AFTER_SALE_HISTORY) },
                    shape = RoundedCornerShape(
                        topStart = 0.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp
                    ),
                )
            }
        }
    )
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
                imeAction = ImeAction.Done, keyboardType = KeyboardType.Text
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


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.page_shipping_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(HomePage.HOME) }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
            ) {
                QrCodeTextField(
                    value = uiState.software.id,
                    onValueChange = { viewModel.setSoftware(uiState.software.copy(id = it)) },
                    onSoftwareChange = { viewModel.setSoftware(it) },
                )
                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(visible = uiState.software.id.isNotEmpty()) {
                    SoftwareCard(software = uiState.software)
                }
            }
        }
    )
}
// endregion

// region Preview
@Preview
@Composable
fun HomePageContentPreview() {
    HomePageContent(
        viewModel = hiltViewModel()
    )
}

@Preview
@Composable
fun ModifyPageContentPreview() {
    ModifyPageContent(uiState = HomeUiState(), navigateTo = {}, onDone = {}, value = "Value"
    )
}

@Preview
@Composable
fun ShippingPageContentPreview() {
    ShippingPageContent(
        uiState = ShippingState(), viewModel = hiltViewModel()
    )
}
// endregion
