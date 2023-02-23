package com.zktony.manager.ui.screen.page

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LaptopMac
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.manager.R
import com.zktony.manager.data.remote.model.Software
import com.zktony.manager.ui.components.*
import com.zktony.manager.ui.screen.HomePage
import com.zktony.manager.ui.screen.SearchReq
import com.zktony.manager.ui.screen.ShippingUiState

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 13:13
 */

// region ShippingPage
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShippingPage(
    modifier: Modifier = Modifier,
    uiState: ShippingUiState,
    navigateTo: (HomePage) -> Unit,
    softwareChange: (Software) -> Unit,
    searchCustomer: () -> Unit,
    searchEquipment: () -> Unit,
    searchReqChange: (SearchReq) -> Unit,
) {
    BackHandler {
        navigateTo(HomePage.HOME)
    }

    Column {
        ManagerAppBar(title = stringResource(id = R.string.page_shipping_title),
            isFullScreen = true,
            onBack = { navigateTo(HomePage.HOME) })

        val lazyListState = rememberLazyListState()

        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .animateContentSize()
                .fillMaxSize(),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                QrCodeTextField(
                    value = uiState.software.id,
                    onValueChange = {
                        softwareChange(
                            uiState.software.copy(
                                id = it,
                                create_by = if (uiState.user != null) uiState.user.name else ""
                            )
                        )
                    },
                    onSoftwareChange = { softwareChange(it.copy(create_by = if (uiState.user != null) uiState.user.name else "")) },
                )
                AnimatedVisibility(visible = uiState.software.id.isNotEmpty()) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        SoftwareCard(
                            software = uiState.software,
                            onClick = { navigateTo(HomePage.SOFTWARE_MODIFY) }
                        )
                    }
                }
            }
            item {
                SearchTextField(
                    label = "客户姓名/手机",
                    value = uiState.searchReq.customer,
                    onValueChange = { searchReqChange(uiState.searchReq.copy(customer = it)) },
                    icon = Icons.Outlined.Person,
                    onSearch = { searchCustomer() },
                    onAdd = { navigateTo(HomePage.CUSTOMER_MODIFY) }
                )

                AnimatedVisibility(visible = uiState.customer != null) {
                    Column {
                        if (uiState.customer != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            CustomerCard(
                                customer = uiState.customer,
                                onClick = { navigateTo(HomePage.CUSTOMER_MODIFY) }
                            )
                        }
                    }
                }
            }

            item {
                SearchTextField(
                    label = "机器名称/型号",
                    value = uiState.searchReq.equipment,
                    onValueChange = { searchReqChange(uiState.searchReq.copy(equipment = it)) },
                    icon = Icons.Outlined.LaptopMac,
                    onSearch = { searchEquipment() },
                    onAdd = { navigateTo(HomePage.EQUIPMENT_MODIFY) }
                )

                AnimatedVisibility(visible = uiState.equipment != null) {
                    Column {
                        if (uiState.equipment != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            EquipmentCard(
                                equipment = uiState.equipment,
                                onClick = { navigateTo(HomePage.EQUIPMENT_MODIFY) }
                            )
                        }
                    }
                }
            }
        }
    }
}
// endregion

// region Preview
@Preview
@Composable
fun ShippingPagePreview() {
    ShippingPage(
        uiState = ShippingUiState(),
        navigateTo = {},
        softwareChange = {},
        searchCustomer = {},
        searchEquipment = {},
        searchReqChange = {},
    )
}
// endregion