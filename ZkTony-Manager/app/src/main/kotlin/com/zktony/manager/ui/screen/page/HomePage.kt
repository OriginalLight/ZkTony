package com.zktony.manager.ui.screen.page

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Shop
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.manager.R
import com.zktony.manager.ui.components.FunctionCard
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.manager.ui.screen.viewmodel.HomePageEnum

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 13:11
 */

// region HomePage
@Composable
fun HomePage(
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
        FunctionCard(
            title = stringResource(id = R.string.page_shipping_title),
            subtitle = stringResource(id = R.string.page_shipping_subtitle),
            icon = Icons.Outlined.LocalShipping,
            onClick = { navigateTo(HomePageEnum.SHIPPING) },
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp
            ),
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp), color = Color.Gray
        )
        FunctionCard(title = stringResource(id = R.string.page_shipping_history_title),
            subtitle = stringResource(id = R.string.page_shipping_history_subtitle),
            icon = Icons.Outlined.History,
            onClick = { navigateTo(HomePageEnum.SHIPPING_HISTORY) })
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp), color = Color.Gray
        )
        FunctionCard(title = stringResource(id = R.string.page_after_sale_title),
            subtitle = stringResource(id = R.string.page_after_sale_subtitle),
            icon = Icons.Outlined.Shop,
            onClick = { navigateTo(HomePageEnum.AFTER_SALE) })
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp), color = Color.Gray
        )
        FunctionCard(
            title = stringResource(id = R.string.page_after_sale_history_title),
            subtitle = stringResource(id = R.string.page_after_sale_history_subtitle),
            icon = Icons.Outlined.History,
            onClick = { navigateTo(HomePageEnum.AFTER_SALE_HISTORY) },
            shape = RoundedCornerShape(
                topStart = 0.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp
            ),
        )
    }
}
// endregion

// region Preview
@Preview
@Composable
fun HomePagePreview() {
    HomePage(navigateTo = {})
}
// endregion