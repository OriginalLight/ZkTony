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
import com.zktony.manager.ui.fragment.*
import com.zktony.manager.ui.utils.ContentType
import com.zktony.manager.ui.viewmodel.ManagerPageEnum
import com.zktony.manager.ui.viewmodel.ManagerUiState
import com.zktony.manager.ui.viewmodel.ManagerViewModel
import org.koin.androidx.compose.koinViewModel

// region: ManagerScreen
@Composable
fun ManagerScreen(
    modifier: Modifier = Modifier,
    contentType: ContentType,
    displayFeatures: List<DisplayFeature>,
    viewModel: ManagerViewModel,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    if (contentType == ContentType.SINGLE_PANE) {
        ManagerScreenSinglePane(
            modifier = modifier,
            uiState = uiState.value,
            viewModel = viewModel,
        )
    } else {
        ManagerScreenDualPane(
            modifier = modifier,
            uiState = uiState.value,
            displayFeatures = displayFeatures,
            viewModel = viewModel,
        )
    }


}
// endregion

// region: ManagerScreenSinglePane
@Composable
fun ManagerScreenSinglePane(
    modifier: Modifier = Modifier,
    uiState: ManagerUiState,
    viewModel: ManagerViewModel,
) {
    AnimatedVisibility(
        visible = uiState.page == ManagerPageEnum.MANAGER,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = shrinkHorizontally { it }
    ) {
        ManagerFragment(
            modifier = modifier,
            navigateTo = viewModel::navigateTo,
        )
    }

    AnimatedVisibility(
        visible = uiState.page == ManagerPageEnum.CUSTOMER_LIST,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = shrinkHorizontally { it }
    ) {
        CustomerListFragment(
            modifier = modifier,
            navigateTo = viewModel::navigateTo,
            viewModel = koinViewModel()
        )
    }

    AnimatedVisibility(
        visible = uiState.page == ManagerPageEnum.CUSTOMER_EDIT,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = shrinkHorizontally { it }
    ) {
        CustomerEditFragment(
            modifier = modifier,
            navigateTo = viewModel::navigateTo,
            viewModel = koinViewModel()
        )
    }

    AnimatedVisibility(
        visible = uiState.page == ManagerPageEnum.INSTRUMENT_LIST,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = shrinkHorizontally { it }
    ) {
        InstrumentListFragment(
            modifier = modifier,
            navigateTo = viewModel::navigateTo,
            viewModel = koinViewModel()
        )
    }

    AnimatedVisibility(
        visible = uiState.page == ManagerPageEnum.INSTRUMENT_EDIT,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = shrinkHorizontally { it }
    ) {
        InstrumentEditFragment(
            modifier = modifier,
            navigateTo = viewModel::navigateTo,
            viewModel = koinViewModel()
        )
    }

    AnimatedVisibility(
        visible = uiState.page == ManagerPageEnum.SOFTWARE_LIST,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = shrinkHorizontally { it }
    ) {
        SoftwareListFragment(
            modifier = modifier,
            navigateTo = viewModel::navigateTo,
            viewModel = koinViewModel()
        )
    }

    AnimatedVisibility(
        visible = uiState.page == ManagerPageEnum.SOFTWARE_EDIT,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = shrinkHorizontally { it }
    ) {
        SoftwareEditFragment(
            modifier = modifier,
            navigateTo = viewModel::navigateTo,
            viewModel = koinViewModel()
        )
    }
}

// region: ManagerScreenDualPane
@Composable
fun ManagerScreenDualPane(
    modifier: Modifier = Modifier,
    uiState: ManagerUiState,
    displayFeatures: List<DisplayFeature>,
    viewModel: ManagerViewModel,
) {
    TwoPane(
        first = {
            ManagerFragment(
                modifier = modifier,
                navigateTo = viewModel::navigateTo,
            )
        },
        second = {
        },
        strategy = HorizontalTwoPaneStrategy(splitFraction = 0.5f, gapWidth = 16.dp),
        displayFeatures = displayFeatures
    )
}
// endregion
