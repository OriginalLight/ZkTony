package com.zktony.manager.ui.screen.page

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.zktony.manager.ui.screen.ShippingState

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 13:13
 */

// region ShippingPage
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShippingPage(
    modifier: Modifier = Modifier,
    uiState: ShippingState,
    navigateTo: (HomePage) -> Unit,
    onSoftwareChange: (Software) -> Unit,
    onSearchCustomer: (String) -> Unit,
    onSearchEquipment: (String) -> Unit,
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
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .animateContentSize()
                .fillMaxSize(),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                QrCodeTextField(
                    value = uiState.software.id,
                    onValueChange = {
                        onSoftwareChange(
                            uiState.software.copy(
                                id = it,
                                create_by = if (uiState.user != null) uiState.user.name else ""
                            )
                        )
                    },
                    onSoftwareChange = { onSoftwareChange(it.copy(create_by = if (uiState.user != null) uiState.user.name else "")) },
                )
            }
            if(uiState.software.id.isNotEmpty()) {
                item {
                    SoftwareCard(
                        software = uiState.software,
                        onClick = { navigateTo(HomePage.SOFTWARE_MODIFY) })
                }
            }
            item {
                var customerSearch by remember { mutableStateOf("") }

                SearchTextField(
                    label = "客户姓名/手机",
                    value = customerSearch,
                    onValueChange = { customerSearch = it },
                    icon = Icons.Outlined.Person,
                    onSearch = { onSearchCustomer(it) },
                    onAdd = { }
                )
            }
            if (uiState.customer != null) {
                item {
                    CustomerCard(
                        customer = uiState.customer,
                        onClick = { }
                    )
                }
            }

            item {
                var equipmentSearch by remember { mutableStateOf("") }

                SearchTextField(
                    label = "机器名称/型号",
                    value = equipmentSearch,
                    onValueChange = { equipmentSearch = it },
                    icon = Icons.Outlined.LaptopMac,
                    onSearch = { onSearchEquipment(it) },
                    onAdd = { }
                )
            }

            if (uiState.equipment != null) {
                item {
                    EquipmentCard(
                        equipment = uiState.equipment,
                        onClick = { }
                    )
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
        uiState = ShippingState(),
        navigateTo = {},
        onSoftwareChange = {},
        onSearchCustomer = {},
        onSearchEquipment = {},
    )
}
// endregion