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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.DisplayFeature
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.zktony.manager.ui.fragment.SettingFragment
import com.zktony.manager.ui.fragment.UpgradeFragment
import com.zktony.manager.ui.fragment.UserEditFragment
import com.zktony.manager.ui.viewmodel.SettingPage
import com.zktony.manager.ui.viewmodel.SettingUiState
import com.zktony.manager.ui.viewmodel.SettingViewModel
import com.zktony.manager.ui.utils.ContentType
import org.koin.androidx.compose.koinViewModel

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
        SettingFragment(
            modifier = modifier,
            navigateTo = viewModel::navigateTo,
        )
    }
    AnimatedVisibility(
        visible = uiState.page == SettingPage.USER_MODIFY,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = shrinkHorizontally { it }
    ) {
        UserEditFragment(
            modifier = modifier,
            navigateTo = viewModel::navigateTo,
            viewModel = koinViewModel(),
        )
    }
    AnimatedVisibility(
        visible = uiState.page == SettingPage.UPGRADE,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = shrinkHorizontally { it }
    ) {
        UpgradeFragment(
            modifier = modifier,
            navigateTo = viewModel::navigateTo,
            viewModel = koinViewModel(),
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
        first = {},
        second = {},
        strategy = HorizontalTwoPaneStrategy(splitFraction = 0.5f, gapWidth = 16.dp),
        displayFeatures = displayFeatures
    )
}
// endregion
