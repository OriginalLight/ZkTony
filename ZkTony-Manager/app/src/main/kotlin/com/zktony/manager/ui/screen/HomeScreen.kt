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
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.DisplayFeature
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.zktony.manager.ui.screen.page.HomePage
import com.zktony.manager.ui.screen.page.ShippingPage
import com.zktony.manager.ui.screen.page.SoftwareModifyPage
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
            uiState = uiState, viewModel = viewModel
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
        HomePage(
            modifier = modifier,
            navigateTo = viewModel::navigateTo
        )
    }
    AnimatedVisibility(
        visible = (uiState.page == HomePage.SHIPPING),
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        ShippingPage(
            modifier = modifier,
            uiState = uiState.shipping,
            navigateTo = viewModel::navigateTo,
            onSoftwareChange = { viewModel.setSoftware(it) },
            onSearchCustomer = { viewModel.searchCustomer(it) },
            onSearchEquipment = { viewModel.searchEquipment(it) },
        )
    }

    AnimatedVisibility(
        visible = (uiState.page == HomePage.SOFTWARE_MODIFY),
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        SoftwareModifyPage(software = uiState.shipping.software,
            navigateTo = viewModel::navigateTo,
            onSoftwareChange = { viewModel.setSoftware(it) })
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
            HomePage(
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


