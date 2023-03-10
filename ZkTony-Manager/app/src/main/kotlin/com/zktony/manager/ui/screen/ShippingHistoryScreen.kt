package com.zktony.manager.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.manager.ui.screen.page.ProductDetailPage
import com.zktony.manager.ui.screen.page.ShippingHistoryPage
import com.zktony.manager.ui.screen.viewmodel.HomePageEnum
import com.zktony.manager.ui.screen.viewmodel.ShippingHistoryPageEnum
import com.zktony.manager.ui.screen.viewmodel.ShippingHistoryViewModel

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 13:13
 */

@Composable
fun ShippingHistoryScreen(
    navigateTo: (HomePageEnum) -> Unit,
    viewModel: ShippingHistoryViewModel,
    isDualPane: Boolean = false
) {
    BackHandler {
        navigateTo(HomePageEnum.HOME)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = uiState.page == ShippingHistoryPageEnum.SHIPPING_HISTORY,
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        ShippingHistoryPage(
            modifier = Modifier,
            uiState = uiState,
            navigateTo = viewModel::navigateTo,
            isDualPane = isDualPane,
            onBack = { navigateTo(HomePageEnum.HOME) },
            onProductClick = { viewModel.productClick(it) },
            onSearch = { viewModel.search() },
            onQueryDtoChange = { viewModel.queryDtoChange(it) },
        )

    }

    AnimatedVisibility(
        visible = (uiState.page == ShippingHistoryPageEnum.PRODUCT_DETAIL),
        enter = expandHorizontally(),
        exit = shrinkHorizontally()
    ) {
        ProductDetailPage(
            modifier = Modifier,
            product = uiState.product,
            software = uiState.software,
            equipment = uiState.equipment,
            customer = uiState.customer,
            onBack = { viewModel.navigateTo(ShippingHistoryPageEnum.SHIPPING_HISTORY) }
        )
    }

}
