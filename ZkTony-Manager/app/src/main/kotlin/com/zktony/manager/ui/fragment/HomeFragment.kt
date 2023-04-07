package com.zktony.manager.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Shop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.manager.R
import com.zktony.manager.ui.components.FeatureCard
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.manager.ui.viewmodel.HomePageEnum

// region HomeFragment

@Composable
fun HomeFragment(
    modifier: Modifier = Modifier,
    navigateTo: (HomePageEnum) -> Unit
) {
    ManagerAppBar(
        title = stringResource(id = R.string.screen_home_title),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyVerticalGrid(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            columns = GridCells.Adaptive(minSize = 128.dp)
        ) {
            item {
                FeatureCard(
                    title = stringResource(id = R.string.page_shipping_title),
                    icon = Icons.Outlined.LocalShipping,
                    onClick = { navigateTo(HomePageEnum.ORDER) }
                )
            }
            item {
                FeatureCard(
                    title = stringResource(id = R.string.page_shipping_history_title),
                    icon = Icons.Outlined.History,
                    onClick = { navigateTo(HomePageEnum.ORDER_HISTORY) }
                )
            }
            item {
                FeatureCard(
                    title = stringResource(id = R.string.page_after_sale_title),
                    icon = Icons.Outlined.Shop,
                    onClick = { navigateTo(HomePageEnum.AFTER_SALE) }
                )
            }
            item {
                FeatureCard(
                    title = stringResource(id = R.string.page_after_sale_history_title),
                    icon = Icons.Outlined.History,
                    onClick = { navigateTo(HomePageEnum.AFTER_SALE_HISTORY) }
                )
            }
        }
    }
}
// endregion


// Preview
@Preview
@Composable
fun HomeFragmentPreview() {
    HomeFragment(navigateTo = {})
}