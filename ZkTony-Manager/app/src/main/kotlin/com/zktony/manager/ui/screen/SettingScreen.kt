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
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.DisplayFeature
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.zktony.manager.R
import com.zktony.manager.ui.components.ProfileImage
import com.zktony.manager.ui.utils.ContentType

// region: SettingScreen
@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    contentType: ContentType,
    displayFeatures: List<DisplayFeature>,
    viewModel: SettingViewModel,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    if (contentType == ContentType.SINGLE_PANE) {
        SettingScreenSinglePane(
            modifier = modifier,
            uiState = uiState.value,
            viewModel = viewModel,
        )
    } else {
        SettingScreenDualPane(
            modifier = modifier,
            uiState = uiState.value,
            displayFeatures = displayFeatures,
            viewModel = viewModel,
        )
    }


}
// endregion

// region: SettingScreenSinglePane
@Composable
fun SettingScreenSinglePane(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    viewModel: SettingViewModel,
) {
    AnimatedVisibility(
        visible = uiState.page == SettingPage.SETTING,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = shrinkHorizontally { it }
    ) {
        SettingPageContent(
            modifier = modifier,
            uiState = uiState,
            navigateTo = viewModel::navigateTo,
        )
    }
    AnimatedVisibility(
        visible = uiState.page == SettingPage.USER_INFO,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = shrinkHorizontally { it }
    ) {
        UserInfoPageContent(
            uiState = uiState,
            navigateTo = viewModel::navigateTo,
            onNameChanged = viewModel::onNameChanged,
            onPhoneChanged = viewModel::onPhoneChanged,
            save = viewModel::save
        )

    }

}
// endregion

// region: SettingScreenDualPane
@Composable
fun SettingScreenDualPane(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    displayFeatures: List<DisplayFeature>,
    viewModel: SettingViewModel,
) {
    TwoPane(
        first = {
            SettingPageContent(
                modifier = modifier,
                uiState = uiState,
                navigateTo = viewModel::navigateTo,
            )
        },
        second = {
            UserInfoPageContent(
                uiState = uiState,
                navigateTo = viewModel::navigateTo,
                onNameChanged = viewModel::onNameChanged,
                onPhoneChanged = viewModel::onPhoneChanged,
                save = viewModel::save
            )
        },
        strategy = HorizontalTwoPaneStrategy(splitFraction = 0.5f, gapWidth = 16.dp),
        displayFeatures = displayFeatures
    )
}
// endregion

// region: SettingPageContent
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPageContent(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    navigateTo: (SettingPage) -> Unit,
) {

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.screen_setting_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
            )
        },
        content = {innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp,
                        focusedElevation = 4.dp,
                        disabledElevation = 0.dp,
                    ),
                    shape = MaterialTheme.shapes.medium,
                    onClick = { navigateTo(SettingPage.USER_INFO) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileImage(
                            drawableResource = R.drawable.avatar_express,
                            description = null
                        )
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(start = 8.dp),
                                text = uiState.user.name.ifEmpty { "预设用户名" },
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                modifier = Modifier
                                    .padding(start = 8.dp),
                                text = uiState.user.phone.ifEmpty { "1345678910" },
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                fontStyle = Italic
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
        }
    )

}
// endregion

// region: UserInfoPageContent
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun UserInfoPageContent(
    uiState: SettingUiState,
    navigateTo: (SettingPage) -> Unit,
    onNameChanged: (String) -> Unit = {},
    onPhoneChanged: (String) -> Unit = {},
    save: () -> Unit = {},
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current

    BackHandler {
        navigateTo(SettingPage.SETTING)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "个人信息",
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigateTo(SettingPage.SETTING) }) {
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
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                TextField(
                    value = uiState.user.name,
                    onValueChange = { onNameChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text(text = "姓名") },
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    shape = CircleShape,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 16.dp),
                        )
                    },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        localFocusManager.moveFocus(focusDirection = FocusDirection.Down)
                    }),
                    visualTransformation = VisualTransformation.None,
                )

                TextField(
                    value = uiState.user.phone,
                    onValueChange = { onPhoneChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text(text = "手机号") },
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    shape = CircleShape,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 16.dp),
                        )
                    },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Phone
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                        save()
                    }),
                    visualTransformation = VisualTransformation.None,
                )

                val transition =
                    updateTransition(targetState = uiState.error != null, label = "shake")
                val shakeOffset by transition.animateDp(
                    transitionSpec = {
                        if (true isTransitioningTo false) {
                            tween(durationMillis = 100)
                        } else {
                            keyframes {
                                durationMillis = 500
                                0.dp at 0
                                10.dp at 100
                                (-10).dp at 200
                                10.dp at 300
                                (-10).dp at 400
                                0.dp at 500
                            }
                        }
                    }, label = "shakeOffset"
                ) { targetState ->
                    if (targetState) 0.dp else 0.dp
                }
                val containerColor by animateColorAsState(targetValue = if (uiState.error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                val contentColor by animateColorAsState(targetValue = if (uiState.error != null) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary)
                Button(
                    onClick = {
                        save()
                        keyboardController?.hide()
                    },
                    enabled = uiState.user.name.isNotEmpty() && uiState.user.phone.isNotEmpty(),
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        .fillMaxWidth(if (uiState.loading) 0.3f else 1f)
                        .height(40.dp)
                        .offset(shakeOffset)
                        .animateContentSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = containerColor,
                        contentColor = contentColor
                    ),
                ) {
                    AnimatedVisibility(visible = uiState.loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    AnimatedVisibility(visible = !uiState.loading) {
                        Text(
                            text = "保存",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    )
}
// endregion

// region: preview
@Preview
@Composable
fun SettingPageContentPreview() {
    SettingPageContent(
        uiState = SettingUiState(),
        navigateTo = { }
    )
}

@Preview
@Composable
fun UserInfoPageContentPreview() {
    UserInfoPageContent(
        uiState = SettingUiState(),
        navigateTo = { }
    )
}
// endregion