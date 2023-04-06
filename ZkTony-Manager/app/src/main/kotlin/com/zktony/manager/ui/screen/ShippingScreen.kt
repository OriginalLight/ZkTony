package com.zktony.manager.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.manager.ui.screen.page.CustomerModifyPage
import com.zktony.manager.ui.screen.page.InstrumentModifyPage
import com.zktony.manager.ui.screen.page.ShippingPage
import com.zktony.manager.ui.screen.page.SoftwareModifyPage
import com.zktony.manager.ui.screen.viewmodel.HomePageEnum
import com.zktony.manager.ui.screen.viewmodel.ShippingPageEnum
import com.zktony.manager.ui.screen.viewmodel.ShippingViewModel
import com.zktony.proto.customer
import com.zktony.proto.instrument

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
            searchCustomer = { viewModel.searchCustomer(it) },
            searchInstrument = { viewModel.searchInstrument(it) },
            saveShipping = { viewModel.saveShipping(it) { navigateTo(HomePageEnum.HOME) } },
            onBack = { navigateTo(HomePageEnum.HOME) }
        )
    }

    AnimatedVisibility(
        visible = (uiState.page == ShippingPageEnum.SOFTWARE_MODIFY),
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        SoftwareModifyPage(
            modifier = Modifier,
            software = uiState.software!!,
            onSave = { viewModel.setSoftware(it) },
            onBack = { viewModel.navigateTo(ShippingPageEnum.SHIPPING) })
    }

    AnimatedVisibility(
        visible = uiState.page == ShippingPageEnum.CUSTOMER_MODIFY,
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        CustomerModifyPage(
            modifier = Modifier,
            customer = uiState.customer ?: customer { },
            onSave = { viewModel.saveCustomer(it, uiState.customer == null) },
            onBack = { viewModel.navigateTo(ShippingPageEnum.SHIPPING) })
    }

    AnimatedVisibility(
        visible = uiState.page == ShippingPageEnum.INSTRUMENT_MODIFY,
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        InstrumentModifyPage(
            modifier = Modifier,
            instrument = uiState.instrument ?: instrument { },
            onSave = { viewModel.saveInstrument(it, uiState.instrument == null) },
            onBack = { viewModel.navigateTo(ShippingPageEnum.SHIPPING) })
    }

}
