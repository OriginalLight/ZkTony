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
import com.zktony.manager.data.remote.model.Customer
import com.zktony.manager.data.remote.model.Equipment
import com.zktony.manager.ui.screen.page.*
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
    val shipping by viewModel.shipping.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = uiState.page == HomePage.HOME,
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        HomePage(
            modifier = modifier,
            navigateTo = viewModel::navigateTo
        )
    }
    AnimatedVisibility(
        visible = uiState.page == HomePage.SHIPPING,
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        ShippingPage(
            modifier = modifier,
            uiState = shipping,
            navigateTo = viewModel::navigateTo,
            softwareChange = { viewModel.setSoftware(it) },
            searchCustomer = { viewModel.searchCustomer() },
            searchEquipment = { viewModel.searchEquipment() },
            searchReqChange = { viewModel.searchReqChange(it) },
            productChange = { viewModel.productChange(it) },
            saveShipping = { viewModel.saveShipping() },
        )
    }

    AnimatedVisibility(
        visible = (uiState.page == HomePage.SOFTWARE_MODIFY),
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        SoftwareModifyPage(software = shipping.software,
            navigateTo = viewModel::navigateTo,
            softwareChange = { viewModel.setSoftware(it) })
    }

    AnimatedVisibility(
        visible = uiState.page == HomePage.CUSTOMER_MODIFY,
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        CustomerModifyPage(
            customer = if (shipping.customer == null) Customer(
                create_by = shipping.user?.name ?: ""
            ) else shipping.customer!!,
            navigateTo = { viewModel.navigateTo(HomePage.SHIPPING) },
            isAdd = shipping.customer == null,
            onDone = {
                if (shipping.customer == null) viewModel.addCustomer(it) else viewModel.updateCustomer(
                    it
                )
            }
        )
    }

    AnimatedVisibility(
        visible = uiState.page == HomePage.EQUIPMENT_MODIFY,
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        EquipmentModifyPage(
            equipment = if (shipping.equipment == null) Equipment(
                create_by = shipping.user?.name ?: ""
            ) else shipping.equipment!!,
            navigateTo = { viewModel.navigateTo(HomePage.SHIPPING) },
            isAdd = shipping.equipment == null,
            onDone = {
                if (shipping.equipment == null) viewModel.addEquipment(it) else viewModel.updateEquipment(
                    it
                )
            }
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
            HomePage(
                modifier = modifier,
                navigateTo = viewModel::navigateTo
            )
        },
        second = {
            val shippingUiState by viewModel.shipping.collectAsStateWithLifecycle()

            AnimatedVisibility(
                visible = (uiState.page == HomePage.SHIPPING || uiState.page == HomePage.HOME),
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                ShippingPage(
                    modifier = modifier,
                    uiState = shippingUiState,
                    navigateTo = viewModel::navigateTo,
                    softwareChange = { viewModel.setSoftware(it) },
                    searchCustomer = { viewModel.searchCustomer() },
                    searchEquipment = { viewModel.searchEquipment() },
                    searchReqChange = { viewModel.searchReqChange(it) },
                    productChange = { viewModel.productChange(it) },
                    saveShipping = { viewModel.saveShipping() },
                )
            }

            AnimatedVisibility(
                visible = (uiState.page == HomePage.SOFTWARE_MODIFY),
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                SoftwareModifyPage(software = shippingUiState.software,
                    navigateTo = viewModel::navigateTo,
                    softwareChange = { viewModel.setSoftware(it) })
            }

            AnimatedVisibility(
                visible = uiState.page == HomePage.CUSTOMER_MODIFY,
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                CustomerModifyPage(
                    customer = if (shippingUiState.customer == null) Customer(
                        create_by = shippingUiState.user?.name ?: ""
                    ) else shippingUiState.customer!!,
                    navigateTo = { viewModel.navigateTo(HomePage.SHIPPING) },
                    isAdd = shippingUiState.customer == null,
                    onDone = {
                        if (shippingUiState.customer == null) viewModel.addCustomer(it) else viewModel.updateCustomer(
                            it
                        )
                    }
                )
            }

            AnimatedVisibility(
                visible = uiState.page == HomePage.EQUIPMENT_MODIFY,
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                EquipmentModifyPage(
                    equipment = if (shippingUiState.equipment == null) Equipment(
                        create_by = shippingUiState.user?.name ?: ""
                    ) else shippingUiState.equipment!!,
                    navigateTo = { viewModel.navigateTo(HomePage.SHIPPING) },
                    isAdd = shippingUiState.equipment == null,
                    onDone = {
                        if (shippingUiState.equipment == null) viewModel.addEquipment(it) else viewModel.updateEquipment(
                            it
                        )
                    }
                )
            }
        },
        strategy = HorizontalTwoPaneStrategy(splitFraction = 0.5f, gapWidth = 16.dp),
        displayFeatures = displayFeatures
    )
}
// endregion


