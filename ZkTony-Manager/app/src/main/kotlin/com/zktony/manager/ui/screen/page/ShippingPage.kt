package com.zktony.manager.ui.screen.page

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.manager.R
import com.zktony.manager.data.remote.model.Product
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
    productChange: (Product) -> Unit,
    searchReqChange: (SearchReq) -> Unit,
    saveShipping: () -> Unit,
) {
    BackHandler {
        navigateTo(HomePage.HOME)
    }

    Column {
        ManagerAppBar(title = stringResource(id = R.string.page_shipping_title),
            isFullScreen = true,
            onBack = { navigateTo(HomePage.HOME) },
            actions = {
                AnimatedVisibility(
                    visible = uiState.software.id.isNotEmpty()
                            && uiState.customer != null
                            && uiState.equipment != null
                            && uiState.product.equipment_number.isNotEmpty()
                            && uiState.product.express_number.isNotEmpty()
                ) {
                    FilledIconButton(
                        onClick = { saveShipping() },
                        modifier = Modifier.padding(8.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        )

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
                Spacer(modifier = Modifier.height(16.dp))
                CodeTextField(
                    label = "软件编号",
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
            item {
                CodeTextField(
                    label = "设备编号",
                    value = uiState.product.equipment_number,
                    onValueChange = {
                        productChange(uiState.product.copy(equipment_number = it))
                    },
                    isQrCode = false,
                )
            }
            item {
                TimeTextField(
                    label = "生产日期",
                    value = uiState.product.equipment_time,
                    onValueChange = { productChange(uiState.product.copy(equipment_time = it)) }
                )

                AnimatedVisibility(visible = uiState.equipment != null) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        if (uiState.equipment != null && uiState.equipment.attachment.isNotEmpty()) {
                            AttachmentCard(
                                attachment = uiState.equipment.attachment,
                                value = uiState.product.attachment,
                                onValueChange = { productChange(uiState.product.copy(attachment = it)) }
                            )
                        }
                    }

                }
            }
            item {
                CodeTextField(
                    label = "快递编号",
                    value = uiState.product.express_number,
                    onValueChange = {
                        productChange(uiState.product.copy(express_number = it))
                    },
                    isQrCode = false,
                )
            }
            item {
                CommonTextField(
                    label = "快递公司",
                    value = uiState.product.express_company,
                    icon = Icons.Outlined.Domain,
                    onValueChange = { productChange(uiState.product.copy(express_company = it)) }
                )
            }
            item {
                CommonTextField(
                    label = "备注说明",
                    value = uiState.product.remarks,
                    icon = Icons.Outlined.Note,
                    singleLine = false,
                    onValueChange = { productChange(uiState.product.copy(remarks = it)) }
                )
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
        productChange = {},
        saveShipping = {}
    )
}
// endregion