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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Shop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.DisplayFeature
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.zktony.manager.R
import com.zktony.manager.data.remote.model.Software
import com.zktony.manager.ui.components.FunctionCard
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.manager.ui.components.QrCodeTextField
import com.zktony.manager.ui.components.SoftwareCard
import com.zktony.manager.ui.utils.ContentType

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
            uiState = uiState,
            viewModel = viewModel
        )
    } else {
        HomeScreenDualPane(
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
    modifier: Modifier = Modifier,
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
            navigateTo = viewModel::navigateTo
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
            navigateTo = viewModel::navigateTo,
            onSoftwareChange = { viewModel.setSoftware(it) }
        )
    }

    AnimatedVisibility(
        visible = (uiState.page == HomePage.SOFTWARE_MODIFY),
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        SoftwareModifyPageContent(
            software = uiState.shipping.software,
            navigateTo = viewModel::navigateTo,
            onSoftwareChange = { viewModel.setSoftware(it) }
        )
    }

}
// endregion

// region HomeScreenDualPane
@Composable
fun HomeScreenDualPane(
    modifier: Modifier = Modifier,
    displayFeatures: List<DisplayFeature>,
    uiState: HomeUiState,
    viewModel: HomeViewModel
) {
    TwoPane(
        first = {
            HomePageContent(
                modifier = modifier,
                navigateTo = viewModel::navigateTo
            )
        },
        second = { /*TODO*/ },
        strategy = HorizontalTwoPaneStrategy(splitFraction = 0.5f, gapWidth = 16.dp),
        displayFeatures = displayFeatures
    )
}
// endregion

/**
 * Pages of the home screen.
 */

// region HomePageContent
@Composable
fun HomePageContent(
    modifier: Modifier = Modifier,
    navigateTo: (HomePage) -> Unit
) {
    ManagerAppBar(
        title = stringResource(id = R.string.screen_home_title),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FunctionCard(
            title = stringResource(id = R.string.page_shipping_title),
            subtitle = stringResource(id = R.string.page_shipping_subtitle),
            icon = Icons.Outlined.LocalShipping,
            onClick = { navigateTo(HomePage.SHIPPING) },
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
            onClick = { navigateTo(HomePage.SHIPPING_HISTORY) })
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp), color = Color.Gray
        )
        FunctionCard(title = stringResource(id = R.string.page_after_sale_title),
            subtitle = stringResource(id = R.string.page_after_sale_subtitle),
            icon = Icons.Outlined.Shop,
            onClick = { navigateTo(HomePage.AFTER_SALE) })
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp), color = Color.Gray
        )
        FunctionCard(
            title = stringResource(id = R.string.page_after_sale_history_title),
            subtitle = stringResource(id = R.string.page_after_sale_history_subtitle),
            icon = Icons.Outlined.History,
            onClick = { navigateTo(HomePage.AFTER_SALE_HISTORY) },
            shape = RoundedCornerShape(
                topStart = 0.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp
            ),
        )
    }
}
// endregion

// region ShippingPageContent
@Composable
fun ShippingPageContent(
    modifier: Modifier = Modifier,
    uiState: ShippingState,
    navigateTo: (HomePage) -> Unit,
    onSoftwareChange: (Software) -> Unit
) {
    BackHandler {
        navigateTo(HomePage.HOME)
    }

    Column {
        ManagerAppBar(
            title = stringResource(id = R.string.page_shipping_title),
            isFullScreen = true,
            onBack = { navigateTo(HomePage.HOME) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
        ) {
            QrCodeTextField(
                value = uiState.software.id,
                onValueChange = { onSoftwareChange(uiState.software.copy(id = it)) },
                onSoftwareChange = { onSoftwareChange(it) },
            )
            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = uiState.software.id.isNotEmpty()) {
                SoftwareCard(
                    software = uiState.software,
                    onClick = { navigateTo(HomePage.SOFTWARE_MODIFY) })
            }
        }
    }
}
// endregion

// region SoftwareModifyPageContent
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SoftwareModifyPageContent(
    modifier: Modifier = Modifier,
    software: Software,
    navigateTo: (HomePage) -> Unit,
    onSoftwareChange: (Software) -> Unit,
) {
    BackHandler {
        navigateTo(HomePage.SHIPPING)
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Column {
        ManagerAppBar(
            title = stringResource(id = R.string.page_software_title),
            isFullScreen = true,
            onBack = { navigateTo(HomePage.SHIPPING) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxSize(),
        ) {
            // 修改光标位置在文字后
            LaunchedEffect(Unit) {
                focusRequester.captureFocus()
            }
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text(text = "包名 : com.example.www") },
                value = software.`package`,
                onValueChange = { onSoftwareChange(software.copy(`package` = it)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        localFocusManager.moveFocus(FocusDirection.Next)
                    }
                ),
                maxLines = 1,
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text(text = "版本名 : xx.xx.xx") },
                value = software.version_name,
                onValueChange = { onSoftwareChange(software.copy(version_name = it)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        localFocusManager.moveFocus(FocusDirection.Next)
                    }
                ),
                maxLines = 1,
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text(text = "版本号 : 1、2、3...") },
                value = software.version_code.toString(),
                onValueChange = {
                    onSoftwareChange(
                        software.copy(
                            version_code = it.toIntOrNull() ?: 1
                        )
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        localFocusManager.moveFocus(FocusDirection.Next)
                    }
                ),
                maxLines = 1,
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text(text = "构建类型 : debug/release") },
                value = software.build_type,
                onValueChange = { onSoftwareChange(software.copy(build_type = it)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        localFocusManager.moveFocus(FocusDirection.Next)
                    }
                ),
                maxLines = 1,
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text(text = "备注说明") },
                value = software.remarks,
                onValueChange = { onSoftwareChange(software.copy(remarks = it)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        navigateTo(HomePage.SHIPPING)
                    }
                ),
                maxLines = 10,
                singleLine = false,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 16.dp),
                    onClick = {
                        onSoftwareChange(
                            Software().copy(
                                `package` = "",
                                version_name = "",
                                version_code = 1,
                                build_type = "",
                                remarks = ""
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                    )
                ) {
                    Text(text = "清空")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 16.dp),
                    onClick = {
                        navigateTo(HomePage.SHIPPING)
                    }
                ) {
                    Text(text = "完成")
                }
            }
        }
    }
}
// endregion

/**
 * Preview
 */

// region Preview
@Preview
@Composable
fun HomePageContentPreview() {
    HomePageContent(
        navigateTo = {}
    )
}

@Preview
@Composable
fun ShippingPageContentPreview() {
    ShippingPageContent(
        uiState = ShippingState(),
        navigateTo = {},
        onSoftwareChange = {}
    )
}

@Preview
@Composable
fun SoftwareModifyPageContentPreview() {
    SoftwareModifyPageContent(software = Software(), navigateTo = {}, onSoftwareChange = {})
}

// endregion
