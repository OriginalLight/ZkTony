package com.zktony.manager.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.manager.data.remote.model.Customer
import com.zktony.manager.data.remote.model.Equipment
import com.zktony.manager.ui.screen.page.CustomerModifyPage
import com.zktony.manager.ui.screen.page.EquipmentModifyPage
import com.zktony.manager.ui.screen.page.ShippingPage
import com.zktony.manager.ui.screen.page.SoftwareModifyPage
import com.zktony.manager.ui.screen.viewmodel.HomePageEnum
import com.zktony.manager.ui.screen.viewmodel.ShippingPageEnum
import com.zktony.manager.ui.screen.viewmodel.ShippingViewModel

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 13:13
 */

@Composable
fun ShippingScreen(
    navigateTo: (HomePageEnum) -> Unit,
    viewModel: ShippingViewModel,
    isDualPane: Boolean = false
) {
    BackHandler {
        navigateTo(HomePageEnum.HOME)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = uiState.page == ShippingPageEnum.SHIPPING,
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        ShippingPage(
            modifier = Modifier,
            uiState = uiState,
            navigateTo = viewModel::navigateTo,
            isDualPane = isDualPane,
            softwareChange = { viewModel.setSoftware(it) },
            searchCustomer = { viewModel.searchCustomer() },
            searchEquipment = { viewModel.searchEquipment() },
            searchReqChange = { viewModel.searchReqChange(it) },
            productChange = { viewModel.productChange(it) },
            saveShipping = { viewModel.saveShipping() },
            onBack = { navigateTo(HomePageEnum.HOME) })
    }

    AnimatedVisibility(
        visible = (uiState.page == ShippingPageEnum.SOFTWARE_MODIFY),
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        SoftwareModifyPage(
            modifier = Modifier,
            software = uiState.software,
            softwareChange = { viewModel.setSoftware(it) },
            onBack = { viewModel.navigateTo(ShippingPageEnum.SHIPPING) })
    }

    AnimatedVisibility(
        visible = uiState.page == ShippingPageEnum.CUSTOMER_MODIFY,
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        CustomerModifyPage(
            modifier = Modifier,
            customer = if (uiState.customer == null) Customer(
                create_by = uiState.user?.name ?: ""
            ) else uiState.customer!!,
            isAdd = uiState.customer == null,
            onDone = { viewModel.saveCustomer(it, uiState.customer == null) },
            onBack = { viewModel.navigateTo(ShippingPageEnum.SHIPPING) })
    }

    AnimatedVisibility(
        visible = uiState.page == ShippingPageEnum.EQUIPMENT_MODIFY,
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        EquipmentModifyPage(
            modifier = Modifier,
            equipment = if (uiState.equipment == null) Equipment(
                create_by = uiState.user?.name ?: ""
            ) else uiState.equipment!!,
            isAdd = uiState.equipment == null,
            onDone = { viewModel.saveEquipment(it, uiState.equipment == null) },
            onBack = { viewModel.navigateTo(ShippingPageEnum.SHIPPING) })
    }

}
